package dev.ankit.platform.product_service.controller;

import dev.ankit.platform.product_service.dto.CreateProductRequest;
import dev.ankit.platform.product_service.dto.ProductResponse;
import dev.ankit.platform.product_service.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest request) {

        log.info("Create product request name={}, price={}",
                request.name(), request.price());

        ProductResponse response = service.create(request);

        log.info("Product created successfully productId={}", response.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable String id) {

        log.info("Fetching product by id={}", id);

        ProductResponse response = service.getById(id);

        log.debug("Product fetched id={}, price={}, stock={}",
                id, response.price(), response.stock());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {

        log.info("Fetching all products");

        List<ProductResponse> response = service.getAll();

        log.info("Products fetched count={}", response.size());

        return ResponseEntity.ok(response);
    }
}
