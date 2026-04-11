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

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {

        log.info("Searching products query='{}', category='{}', page={}, size={}",
                query, category, page, size);

        List<ProductResponse> response = service.search(query, category, page, size);

        log.info("Product search completed results={}", response.size());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search/reindex")
    public ResponseEntity<Void> reindexSearchCatalog() {
        log.info("Manual request received to reindex product catalog into Elasticsearch");
        service.reindexSearchCatalog();
        return ResponseEntity.accepted().build();
    }
}
