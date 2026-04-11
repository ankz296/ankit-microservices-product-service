package dev.ankit.platform.product_service.search;

import dev.ankit.platform.product_service.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductSearchSyncRunner implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductSearchService productSearchService;
    private final boolean reindexOnStartup;

    public ProductSearchSyncRunner(
            ProductRepository productRepository,
            ProductSearchService productSearchService,
            @Value("${product.search.reindex-on-startup:false}") boolean reindexOnStartup
    ) {
        this.productRepository = productRepository;
        this.productSearchService = productSearchService;
        this.reindexOnStartup = reindexOnStartup;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!reindexOnStartup) {
            return;
        }

        log.info("Startup reindex to Elasticsearch started");
        productSearchService.reindexAll(productRepository.findAll());
    }
}
