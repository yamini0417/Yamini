package com.glowhub.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationResponse {
    private String advice;

    @JsonProperty("recommended_products")
    private List<Product> recommendedProducts;

    @JsonProperty("routine_steps")
    private List<String> routineSteps;
}
