package com.glowhub.controller;

import com.glowhub.model.Product;
import com.glowhub.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String gender,
        @RequestParam(required = false) String concern,
        @RequestParam(required = false) String subcategory,
        @RequestParam(name = "min_rating", required = false) Double minRating,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        return productService.getProducts(category, gender, concern, subcategory, minRating, limit, offset);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return productService.getProductById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/search/query")
    public List<Product> searchProducts(@RequestParam String q) {
        return productService.searchProducts(q);
    }

    @GetMapping("/concerns")
    public List<String> getConcerns() {
        return productService.getAllConcerns();
    }

    @GetMapping("/sources")
    public List<String> getSources() {
        return productService.getAllSources();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return productService.getStats();
    }
}
