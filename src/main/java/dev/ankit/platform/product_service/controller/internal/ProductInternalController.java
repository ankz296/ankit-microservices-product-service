package dev.ankit.platform.product_service.controller.internal;

import dev.ankit.platform.product_service.dto.ProductResponse;
import dev.ankit.platform.product_service.dto.internal.ProductInternalDto;
import dev.ankit.platform.product_service.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/products")
public class ProductInternalController {

    private final ProductService productService;

    public ProductInternalController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductInternalDto> getProduct(@PathVariable String productId) {

        log.info("Internal request: fetch product productId={}", productId);

        ProductResponse p = productService.getById(productId);

        log.debug("Internal product fetched productId={}, price={}, stock={}",
                productId, p.price(), p.stock());

        return ResponseEntity.ok(new ProductInternalDto(
                p.id(),
                true,
                p.price(),
                p.stock()
        ));
    }
}