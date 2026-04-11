package dev.ankit.platform.product_service.services;

import dev.ankit.platform.product_service.config.RedisConfig;
import dev.ankit.platform.product_service.domain.Product;
import dev.ankit.platform.product_service.dto.CreateProductRequest;
import dev.ankit.platform.product_service.dto.ProductResponse;
import dev.ankit.platform.product_service.exception.ResourceNotFoundException;
import dev.ankit.platform.product_service.repository.ProductRepository;
import dev.ankit.platform.product_service.search.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductSearchService productSearchService;

    public ProductService(ProductRepository repository, ProductSearchService productSearchService) {
        this.repository = repository;
        this.productSearchService = productSearchService;
    }

    @CacheEvict(cacheNames = RedisConfig.PRODUCTS_ALL_CACHE, allEntries = true)
    public ProductResponse create(CreateProductRequest request) {
        log.info("Evicting product list cache after create request");
        log.info("Creating product name={}, price={}, stock={}",
                request.name(), request.price(), request.stock());

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .category(request.category())
                .createdAt(Instant.now())
                .build();

        Product saved = repository.save(product);
        productSearchService.indexProduct(saved);

        log.info("Product created successfully productId={}", saved.getId());
        return mapToResponse(saved);
    }

    @Cacheable(cacheNames = RedisConfig.PRODUCTS_BY_ID_CACHE, key = "#id")
    public ProductResponse getById(String id) {
        log.info("Cache miss for product id={}, querying Mongo", id);

        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found id={}", id);
                    return new ResourceNotFoundException("Product not found: " + id);
                });

        return mapToResponse(product);
    }

    @Cacheable(cacheNames = RedisConfig.PRODUCTS_ALL_CACHE)
    public List<ProductResponse> getAll() {
        log.info("Cache miss for product list, querying Mongo");

        List<ProductResponse> products = repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Products fetched count={}", products.size());
        return products;
    }

    public List<ProductResponse> search(String query, String category, int page, int size) {
        log.info("Searching products via Elasticsearch query='{}', category='{}', page={}, size={}",
                query, category, page, size);

        return productSearchService.search(query, category, page, size);
    }

    public void reindexSearchCatalog() {
        log.info("Starting manual full reindex of product catalog into Elasticsearch");
        productSearchService.reindexAll(repository.findAll());
    }

    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(String id, CreateProductRequest request) {
        log.info("Cache EVICT - product {} updated", id);

        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());

        Product updated = repository.save(product);
        productSearchService.indexProduct(updated);

        return mapToResponse(updated);
    }

    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product-list", allEntries = true)
    })
    public void deleteProduct(String id) {
        log.info("Cache EVICT - product {} deleted", id);
        repository.deleteById(id);
        productSearchService.deleteProduct(id);
    }

    @CacheEvict(value = {"products", "product-list"}, allEntries = true)
    public void evictAllProductCaches() {
        log.info("Cache FULL EVICT - all product caches cleared");
    }

    private ProductResponse mapToResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getCategory()
        );
    }
}
