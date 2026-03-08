package dev.ankit.platform.product_service.services;


import dev.ankit.platform.product_service.domain.Product;
import dev.ankit.platform.product_service.dto.CreateProductRequest;
import dev.ankit.platform.product_service.dto.ProductResponse;
import dev.ankit.platform.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductResponse create(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .category(request.category())
                .createdAt(Instant.now())
                .build();

        Product saved = repository.save(product);
        return mapToResponse(saved);
    }

    public ProductResponse getById(String id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    public List<ProductResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
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
