package com.glowhub.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private String brand;
    private String category;
    private String subcategory;
    private String gender;
    private List<String> concerns;
    private String description;
    private String price;
    private double rating;
    private int reviews;
    private String image;
    private String source;

    @JsonProperty("source_url")
    private String sourceUrl;

    private List<String> ingredients;

    @JsonProperty("key_benefits")
    private List<String> keyBenefits;
}
