package com.glowhub.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AIRecommendationRequest {
    private String problem;
    private String gender;

    @JsonProperty("age_range")
    private String ageRange;

    @JsonProperty("skin_type")
    private String skinType;

    @JsonProperty("hair_type")
    private String hairType;
}
