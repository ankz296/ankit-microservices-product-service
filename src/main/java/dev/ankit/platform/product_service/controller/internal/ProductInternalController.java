package dev.ankit.platform.product_service.controller.internal;

import dev.ankit.platform.product_service.dto.ProductResponse;
import dev.ankit.platform.product_service.dto.internal.ProductInternalDto;
import dev.ankit.platform.product_service.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/products")
public class ProductInternalController {

    private final ProductService productService;

    public ProductInternalController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductInternalDto> getProduct(@PathVariable String productId) {
        ProductResponse p = productService.getById(productId); // should throw if not found
        return ResponseEntity.ok(new ProductInternalDto(
                p.id(),
                true,       // or available flag; adapt to your model
                // ensure you have it; else return 0
                p.price(),
                p.stock()// ensure you have it; else return 0
        ));
    }
}