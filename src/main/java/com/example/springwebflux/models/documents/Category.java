package com.example.springwebflux.models.documents;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  @Id
  private String id;

  @NotEmpty
  private String name;

  public Category(String name) {
    this.name = name;
  }
}
