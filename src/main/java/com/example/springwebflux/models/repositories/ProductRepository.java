package com.example.springwebflux.models.repositories;

import com.example.springwebflux.models.documents.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

  public Flux<Product> findByNameLikeIgnoreCase(String name);
}
