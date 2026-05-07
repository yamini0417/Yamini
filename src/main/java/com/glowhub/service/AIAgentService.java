package com.glowhub.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glowhub.model.AIRecommendationRequest;
import com.glowhub.model.AIRecommendationResponse;
import com.glowhub.model.ChatMessage;
import com.glowhub.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIAgentService {

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private AnthropicClient client;

    public AIAgentService(ObjectMapper objectMapper, ProductService productService) {
        this.objectMapper = objectMapper;
        this.productService = productService;
    }

    @PostConstruct
    public void init() {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            client = AnthropicOkHttpClient.fromEnv();
        }
    }

    public boolean isConfigured() {
        return client != null;
    }

    public AIRecommendationResponse getRecommendations(AIRecommendationRequest request) throws Exception {
        List<Product> all = productService.getAllProducts();

        // Build a compact catalog for the prompt
        List<Map<String, Object>> catalog = all.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("brand", p.getBrand());
            m.put("category", p.getCategory());
            m.put("subcategory", p.getSubcategory());
            m.put("gender", p.getGender());
            m.put("concerns", p.getConcerns());
            m.put("price", p.getPrice());
            m.put("rating", p.getRating());
            m.put("key_benefits", p.getKeyBenefits());
            return m;
        }).collect(Collectors.toList());

        String catalogJson = objectMapper.writeValueAsString(catalog);

        StringBuilder userCtx = new StringBuilder("Problem/concern: ").append(request.getProblem());
        if (request.getGender() != null) userCtx.append("\nGender: ").append(request.getGender());
        if (request.getAgeRange() != null) userCtx.append("\nAge range: ").append(request.getAgeRange());
        if (request.getSkinType() != null) userCtx.append("\nSkin type: ").append(request.getSkinType());
        if (request.getHairType() != null) userCtx.append("\nHair type: ").append(request.getHairType());

        String systemPrompt = """
            You are an expert beauty advisor specializing in skincare and haircare for all genders.

            When given a user's beauty concern and a product catalog, you must:
            1. Provide empathetic, expert advice addressing their specific problem
            2. Select the most relevant product IDs from the catalog (2-5 products max)
            3. Create a simple daily/weekly routine with steps

            Always respond in valid JSON with this exact structure:
            {
                "advice": "Expert advice text (2-3 paragraphs)",
                "recommended_product_ids": ["id1", "id2"],
                "routine_steps": ["Step 1: ...", "Step 2: ..."]
            }

            Only recommend products from the provided catalog. Be specific and helpful.""";

        String userMessage = "Product catalog:\n" + catalogJson + "\n\nUser context:\n" + userCtx;

        Message response = client.messages().create(
            MessageCreateParams.builder()
                .model("claude-sonnet-4-6")
                .maxTokens(1500L)
                .system(systemPrompt)
                .addUserMessage(userMessage)
                .build()
        );

        String raw = extractText(response).trim();
        if (raw.startsWith("```")) {
            raw = raw.replaceAll("^```json?\\s*", "").replaceAll("```$", "").trim();
        }

        JsonNode json = objectMapper.readTree(raw);
        String advice = json.get("advice").asText();

        List<String> ids = new ArrayList<>();
        json.get("recommended_product_ids").forEach(n -> ids.add(n.asText()));

        List<String> steps = new ArrayList<>();
        json.get("routine_steps").forEach(n -> steps.add(n.asText()));

        Map<String, Product> productMap = all.stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

        List<Product> recommended = ids.stream()
            .filter(productMap::containsKey)
            .map(productMap::get)
            .collect(Collectors.toList());

        return new AIRecommendationResponse(advice, recommended, steps);
    }

    public String chat(String message, List<ChatMessage> history) {
        List<Product> all = productService.getAllProducts();

        List<Map<String, Object>> catalog = all.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("brand", p.getBrand());
            m.put("category", p.getCategory());
            m.put("concerns", p.getConcerns());
            m.put("price", p.getPrice());
            m.put("gender", p.getGender());
            return m;
        }).collect(Collectors.toList());

        String catalogJson;
        try {
            catalogJson = objectMapper.writeValueAsString(catalog);
        } catch (Exception e) {
            catalogJson = "[]";
        }

        String systemPrompt = """
            You are BeautyAI, a friendly and knowledgeable beauty consultant for GlowHub.

            You help users with:
            - Skincare concerns (acne, dryness, oily skin, aging, dark spots, sensitivity, etc.)
            - Haircare concerns (dandruff, hair loss, dryness, frizz, damage, etc.)
            - Product recommendations from our curated catalog
            - Beauty routines and tips for men and women

            Available products catalog:
            """ + catalogJson + """

            When recommending products, mention the product name and ID.
            Be warm, professional, and give personalized advice.
            Keep responses concise but helpful (2-4 sentences for simple questions).""";

        MessageCreateParams.Builder builder = MessageCreateParams.builder()
            .model("claude-sonnet-4-6")
            .maxTokens(800L)
            .system(systemPrompt);

        if (history != null) {
            for (ChatMessage msg : history) {
                if ("user".equals(msg.getRole())) {
                    builder.addUserMessage(msg.getContent());
                } else if ("assistant".equals(msg.getRole())) {
                    builder.addAssistantMessage(msg.getContent());
                }
            }
        }
        builder.addUserMessage(message);

        Message response = client.messages().create(builder.build());
        return extractText(response);
    }

    private String extractText(Message message) {
        return message.content().stream()
            .filter(b -> b.isText())
            .findFirst()
            .map(b -> b.asText().text())
            .orElse("");
    }
}
