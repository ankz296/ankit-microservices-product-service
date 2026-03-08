package dev.ankit.platform.product_service.repository;

import dev.ankit.platform.product_service.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}