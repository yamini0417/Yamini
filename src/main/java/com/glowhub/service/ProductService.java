package com.glowhub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glowhub.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ObjectMapper objectMapper;
    private List<Product> products = new ArrayList<>();

    public ProductService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadProducts() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/products.json");
        products = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        System.out.println("Loaded " + products.size() + " products");
    }

    public List<Product> getProducts(String category, String gender, String concern,
                                     String subcategory, Double minRating, int limit, int offset) {
        return products.stream()
            .filter(p -> isNullOrAll(category) || p.getCategory().equalsIgnoreCase(category))
            .filter(p -> isNullOrAll(gender) ||
                         p.getGender().equalsIgnoreCase(gender) ||
                         "unisex".equalsIgnoreCase(p.getGender()))
            .filter(p -> concern == null || concern.isEmpty() ||
                         p.getConcerns().stream()
                             .anyMatch(c -> c.toLowerCase().contains(concern.toLowerCase())))
            .filter(p -> subcategory == null || subcategory.isEmpty() ||
                         p.getSubcategory().equalsIgnoreCase(subcategory))
            .filter(p -> minRating == null || p.getRating() >= minRating)
            .sorted(Comparator.comparingDouble(Product::getRating)
                              .thenComparingInt(Product::getReviews)
                              .reversed())
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());
    }

    public Optional<Product> getProductById(String id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<Product> searchProducts(String query) {
        String q = query.toLowerCase();
        return products.stream()
            .map(p -> {
                int score = 0;
                if (p.getName().toLowerCase().contains(q))        score += 3;
                if (p.getBrand().toLowerCase().contains(q))       score += 2;
                if (p.getDescription().toLowerCase().contains(q)) score += 1;
                if (p.getConcerns().stream().anyMatch(c -> c.toLowerCase().contains(q))) score += 2;
                if (p.getKeyBenefits().stream().anyMatch(b -> b.toLowerCase().contains(q))) score += 1;
                if (p.getCategory().toLowerCase().contains(q) ||
                    p.getSubcategory().toLowerCase().contains(q)) score += 2;
                return new AbstractMap.SimpleEntry<>(score, p);
            })
            .filter(e -> e.getKey() > 0)
            .sorted(Map.Entry.<Integer, Product>comparingByKey().reversed())
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    public List<String> getAllConcerns() {
        return products.stream()
            .flatMap(p -> p.getConcerns().stream())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    public List<String> getAllSources() {
        return products.stream()
            .map(Product::getSource)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total_products", products.size());
        stats.put("skincare_count", products.stream().filter(p -> "skincare".equals(p.getCategory())).count());
        stats.put("haircare_count", products.stream().filter(p -> "haircare".equals(p.getCategory())).count());
        stats.put("sources", products.stream().map(Product::getSource).distinct().count());
        stats.put("brands", products.stream().map(Product::getBrand).distinct().count());
        return stats;
    }

    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(products);
    }

    private boolean isNullOrAll(String value) {
        return value == null || value.isEmpty() || "all".equalsIgnoreCase(value);
    }
}
