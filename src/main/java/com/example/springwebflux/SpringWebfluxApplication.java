package com.example.springwebflux;

import com.example.springwebflux.models.documents.Category;
import com.example.springwebflux.models.documents.Product;
import com.example.springwebflux.services.ProductService;
import java.net.URI;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class SpringWebfluxApplication implements CommandLineRunner {

  @Autowired
  private ProductService productService;

  @Autowired
  private ReactiveMongoTemplate mongoTemplate;

  public static void main(String[] args) {
    SpringApplication.run(SpringWebfluxApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    mongoTemplate.dropCollection("products").subscribe();
    mongoTemplate.dropCollection("categories").subscribe();

    Category electronics = new Category("Electronics");
    Category food = new Category("Food");
    Category clothing = new Category("Clothing");
    Category sports = new Category("Sports");

    Flux.just(electronics, food, clothing, sports)
        .flatMap(productService::saveCategory)
        .doOnNext(c -> {
          log.info("Saved category: {}", c.getName());
        })
        .thenMany(Flux.just(
                new Product("iPhone X", 999.0, electronics),
                new Product("Macbook Pro", 1299.0, electronics),
                new Product("Pizza", 4.0, food),
                new Product("Jeans", 59.0, clothing),
                new Product("Running Shoes", 89.0, sports)
            ).flatMap(p -> {
              p.setCreatedAt(new Date());
              return productService.save(p);
            })
        ).subscribe(p -> {
          log.info("Saved product: {}", p.getName());
        });
  }


}
