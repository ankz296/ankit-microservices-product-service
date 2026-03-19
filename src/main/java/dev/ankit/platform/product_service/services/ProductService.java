package dev.ankit.platform.product_service.services;


import dev.ankit.platform.product_service.domain.Product;
import dev.ankit.platform.product_service.dto.CreateProductRequest;
import dev.ankit.platform.product_service.dto.ProductResponse;
import dev.ankit.platform.product_service.exception.ResourceNotFoundException;
import dev.ankit.platform.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductResponse create(CreateProductRequest request) {

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

        log.info("Product created successfully productId={}", saved.getId());

        return mapToResponse(saved);
    }

    public ProductResponse getById(String id) {

        log.debug("Fetching product id={}", id);

        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found id={}", id);
                    return new ResourceNotFoundException("Product not found: " + id);
                });

        return mapToResponse(product);
    }

    public List<ProductResponse> getAll() {

        log.debug("Fetching all products");

        List<ProductResponse> products = repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Products fetched count={}", products.size());

        return products;
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
