package com.example.springwebflux.services;


import com.example.springwebflux.models.documents.Category;
import com.example.springwebflux.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

  public Mono<Product> save(Product product);

  public Mono<Product> findById(String id);

  public Flux<Product> findByName(String name);

  public Flux<Product> findAll();

  public Mono<Void> delete(Product id);

  public Mono<Category> saveCategory(Category category);
}
