package com.example.springwebflux.handlers;

import com.example.springwebflux.models.documents.Category;
import com.example.springwebflux.models.documents.Product;
import com.example.springwebflux.services.ProductService;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

  private final ProductService service;

  private final Validator validator;

  @Value("${config.uploads.path}")
  private String path;

  public ProductHandler(ProductService service, Validator validator) {
    this.service = service;
    this.validator = validator;
  }

  public Mono<ServerResponse> upload(ServerRequest request) {
    String id = request.pathVariable("id");
    return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
        .cast(FilePart.class).flatMap(filePart -> service.findById(id).flatMap(product -> {
          product.setPhoto(
              UUID.randomUUID() + "-" + filePart.filename().replace(" ", "").replace(":", "")
                  .replace("\\", ""));
          return filePart.transferTo(new File(path + product.getPhoto()))
              .then(service.save(product));
        })).flatMap(
            p -> ServerResponse.created(URI.create("/api/v2/products/upload/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(p))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> createWithPhoto(ServerRequest request) {

    Mono<Product> productMono = request.multipartData().map(multipart -> {
      FormFieldPart name = (FormFieldPart) multipart.toSingleValueMap().get("name");
      FormFieldPart price = (FormFieldPart) multipart.toSingleValueMap().get("price");
      FormFieldPart categoryId = (FormFieldPart) multipart.toSingleValueMap().get("category.id");
      FormFieldPart categoryName = (FormFieldPart) multipart.toSingleValueMap()
          .get("category.name");

      Category category = new Category();
      category.setId(categoryId.value());
      category.setName(categoryName.value());
      return new Product(name.value(), Double.parseDouble(price.value()), category);

    });

    return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
        .cast(FilePart.class).flatMap(filePart -> productMono.flatMap(product -> {
          product.setPhoto(
              UUID.randomUUID() + "-" + filePart.filename().replace(" ", "").replace(":", "")
                  .replace("\\", ""));
          product.setCreatedAt(new Date());
          return filePart.transferTo(new File(path + product.getPhoto()))
              .then(service.save(product));
        })).flatMap(
            p -> ServerResponse.created(URI.create("/api/v2/products/upload/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(p))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> list(ServerRequest request) {
    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
        .body(service.findAll(), Product.class);
  }

  public Mono<ServerResponse> ver(ServerRequest request) {

    String id = request.pathVariable("id");

    return service.findById(id)
        .flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(p))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    Mono<Product> productMono = request.bodyToMono(Product.class);

    return productMono.flatMap(p -> {

      Errors errors = new BeanPropertyBindingResult(p, Product.class.getName());
      validator.validate(p, errors);

      if (errors.hasErrors()) {
        return Flux.fromIterable(errors.getFieldErrors()).map(
                fieldError -> "El campo " + fieldError.getField() + " "
                    + fieldError.getDefaultMessage()).collectList()
            .flatMap(list -> ServerResponse.badRequest().bodyValue(list));
      } else {

        if (p.getCreatedAt() == null) {
          p.setCreatedAt(new Date());
        }
        return service.save(p).flatMap(
            pdb -> ServerResponse.created(URI.create("/api/v2/products/".concat(pdb.getId())))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(pdb));
      }
    });

  }


  public Mono<ServerResponse> edit(ServerRequest request) {

    Mono<Product> productMono = request.bodyToMono(Product.class);
    String id = request.pathVariable("id");

    Mono<Product> productDb = service.findById(id);

    return productDb.zipWith(productMono, (db, req) -> {
          db.setName(req.getName());
          db.setPrice(req.getPrice());
          db.setCategory(req.getCategory());
          return db;
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products".concat(p.getId())))
            .contentType(MediaType.APPLICATION_JSON).body(service.save(p), Product.class))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> delete(ServerRequest request) {

    String id = request.pathVariable("id");

    Mono<Product> productMono = service.findById(id);

    return productMono.flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
        .switchIfEmpty(ServerResponse.notFound().build());
  }
}
