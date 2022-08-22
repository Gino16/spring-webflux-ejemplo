package com.example.springwebflux;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.example.springwebflux.handlers.ProductHandler;
import com.example.springwebflux.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@AllArgsConstructor
public class RouterFunctionConfig {

  private ProductService service;

  @Bean
  public RouterFunction<ServerResponse> routes(ProductHandler handler) {
    return route(GET("/api/v2/products").or(GET("/api/v3/products")), handler::list).andRoute(
            GET("/api/v2/products/{id}"), handler::ver)
        .andRoute(POST("/api/v2/products")/*.and(contentType(MediaType.APPLICATION_JSON))*/,
            handler::create).andRoute(PUT("/api/v2/products/{id}"), handler::edit)
        .andRoute(DELETE("/api/v2/products/{id}"), handler::delete)
        .andRoute(POST("/api/v2/products/upload/{id}"), handler::upload)
        .andRoute(POST("/api/v2/products/create"), handler::createWithPhoto);
  }
}
