package com.example.springwebflux.controllers;

import com.example.springwebflux.models.documents.Product;
import com.example.springwebflux.services.ProductService;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private ProductService productService;

  @Value("${config.uploads.path}")
  private String path;

  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping("/v2")
  public Mono<ResponseEntity<Product>> createWithPhoto(Product product,
      @RequestPart FilePart filePart) {
    if (product.getCreatedAt() == null) {
      product.setCreatedAt(new Date());
    }

    product.setPhoto(
        UUID.randomUUID() + "-" + filePart.filename().replace(" ", "").replace(":", "")
            .replace("\\", ""));

    return filePart.transferTo(new File(path + product.getPhoto()))
        .then(productService.save(product)).map(
            p -> ResponseEntity.created(URI.create("/api/products/" + p.getId()))
                .contentType(MediaType.APPLICATION_JSON).body(p));
  }

  @PostMapping("/upload/{id}")
  public Mono<ResponseEntity<Product>> upload(@PathVariable String id,
      @RequestPart FilePart filePart) {
    return productService.findById(id).flatMap(p -> {
      p.setPhoto(
          UUID.randomUUID() + "-" + filePart.filename().replace(" ", "").replace(":", "")
              .replace("\\", ""));
      return filePart.transferTo(new File(path + p.getPhoto())).then(productService.save(p));
    }).map(ResponseEntity::ok);
  }

  @GetMapping
  public Mono<ResponseEntity<Flux<Product>>> list() {
    return Mono.just(
        ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(productService.findAll()));
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<Product>> findById(@PathVariable String id) {
    return productService.findById(id)
        .map(product -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(product))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  public Mono<ResponseEntity<Map<String, Object>>> create(
      @Valid @RequestBody Mono<Product> monoProduct) {
    Map<String, Object> response = new HashMap<>();
    return monoProduct.flatMap(product -> {
          if (product.getCreatedAt() == null) {
            product.setCreatedAt(new Date());
          }

          return productService.save(product).map(p -> {
            response.put("product", p);
            response.put("message", "Product saved");
            return ResponseEntity.created(URI.create("/api/products/" + p.getId()))
                .contentType(MediaType.APPLICATION_JSON).body(response);
          });
        })
        .onErrorResume(t -> Mono.just(t).cast(WebExchangeBindException.class)
            .flatMap(e -> Mono.just(e.getFieldErrors()))
            .flatMapMany(Flux::fromIterable)
            .map(fieldError -> "El campo " + fieldError.getField() + " "
                + fieldError.getDefaultMessage())
            .collectList()
            .flatMap(list -> {
              response.put("errors", list);
              response.put("timestamp", new Date());
              response.put("status", HttpStatus.BAD_REQUEST.value());
              return Mono.just(ResponseEntity.badRequest().body(response));
            }));


  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<Product>> edit(@RequestBody Product product, @PathVariable String id) {
    return productService.findById(id).flatMap(product1 -> {
          product1.setName(product.getName());
          product1.setPrice(product.getPrice());
          product1.setCategory(product.getCategory());
          return productService.save(product1);
        }).map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId())))
            .contentType(MediaType.APPLICATION_JSON).body(p))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Object>> delete(@PathVariable String id) {
    return productService.findById(id)
        .flatMap(p -> productService.delete(p).then(Mono.just(ResponseEntity.noContent().build())))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }
}
