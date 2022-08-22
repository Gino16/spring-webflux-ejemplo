package com.example.springwebflux.services.impl;


import com.example.springwebflux.models.documents.Category;
import com.example.springwebflux.models.documents.Product;
import com.example.springwebflux.models.repositories.CategoryRepository;
import com.example.springwebflux.models.repositories.ProductRepository;
import com.example.springwebflux.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  private final CategoryRepository categoryRepository;

  @Override
  public Mono<Product> save(Product product) {
    return productRepository.save(product);
  }

  @Override
  public Mono<Product> findById(String id) {
    return productRepository.findById(id);
  }

  @Override
  public Flux<Product> findByName(String name) {
    return productRepository.findByNameLikeIgnoreCase(name);
  }

  @Override
  public Flux<Product> findAll() {
    return productRepository.findAll();
  }

  @Override
  public Mono<Void> delete(Product id) {
    return productRepository.delete(id);
  }

  @Override
  public Mono<Category> saveCategory(Category category) {
    return categoryRepository.save(category);
  }
}
