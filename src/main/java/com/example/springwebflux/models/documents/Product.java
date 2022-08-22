package com.example.springwebflux.models.documents;

import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@ToString
public class Product {

  @Id
  private String id;

  @NotEmpty
  @NotBlank
  private String name;

  @NotNull
  private Double price;

  private String photo;

  private Date createdAt;

  @Valid
  @NotNull
  private Category category;

  public Product(String name, Double price, Category category) {
    this.name = name;
    this.price = price;
    this.category = category;
  }
}
