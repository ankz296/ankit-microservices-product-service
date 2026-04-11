package dev.ankit.platform.product_service.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import dev.ankit.platform.product_service.domain.Product;
import dev.ankit.platform.product_service.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class ProductSearchService {

    private final ProductSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final boolean searchEnabled;

    public ProductSearchService(
            ProductSearchRepository searchRepository,
            ElasticsearchOperations elasticsearchOperations,
            @Value("${product.search.enabled:true}") boolean searchEnabled
    ) {
        this.searchRepository = searchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.searchEnabled = searchEnabled;
    }

    public void indexProduct(Product product) {
        if (!searchEnabled) {
            return;
        }

        try {
            searchRepository.save(ProductSearchDocument.from(product));
            log.info("Product indexed in Elasticsearch productId={}", product.getId());
        } catch (Exception ex) {
            log.error("Failed to index product in Elasticsearch productId={}, error={}",
                    product.getId(), ex.getMessage(), ex);
        }
    }

    public void deleteProduct(String id) {
        if (!searchEnabled) {
            return;
        }

        try {
            searchRepository.deleteById(id);
            log.info("Product removed from Elasticsearch index productId={}", id);
        } catch (Exception ex) {
            log.error("Failed to remove product from Elasticsearch index productId={}, error={}",
                    id, ex.getMessage(), ex);
        }
    }

    public List<ProductResponse> search(String queryText, String category, int page, int size) {
        if (!searchEnabled) {
            log.warn("Product search requested while Elasticsearch integration is disabled");
            return List.of();
        }

        Pageable pageable = PageRequest.of(page, size);

        NativeQuery query = NativeQuery.builder()
                .withQuery(query1 -> query1.bool(bool -> {
                    if (StringUtils.hasText(queryText)) {
                        bool.must(must -> must.multiMatch(mm -> mm
                                .query(queryText)
                                .fields("name", "description", "category")
                                .operator(Operator.Or)));
                    } else {
                        bool.must(must -> must.matchAll(matchAll -> matchAll));
                    }

                    if (StringUtils.hasText(category)) {
                        bool.filter(filter -> filter.term(term -> term
                                .field("category")
                                .value(category)));
                    }

                    return bool;
                }))
                .withPageable(pageable)
                .build();

        List<ProductResponse> results = elasticsearchOperations.search(query, ProductSearchDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .map(this::mapToResponse)
                .toList();

        log.info("Elasticsearch product search completed query='{}', category='{}', results={}",
                queryText, category, results.size());

        return results;
    }

    public void reindexAll(List<Product> products) {
        if (!searchEnabled) {
            return;
        }

        try {
            Iterable<ProductSearchDocument> documents = products.stream()
                    .map(ProductSearchDocument::from)
                    .toList();

            searchRepository.saveAll(documents);

            log.info("Full product reindex completed count={}", products.size());
        } catch (Exception ex) {
            log.error("Failed to reindex products into Elasticsearch error={}",
                    ex.getMessage(), ex);
        }
    }

    private ProductResponse mapToResponse(ProductSearchDocument document) {
        return new ProductResponse(
                document.getId(),
                document.getName(),
                document.getDescription(),
                document.getPrice(),
                document.getStock(),
                document.getCategory()
        );
    }
}
