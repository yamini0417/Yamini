package com.glowhub.controller;

import com.glowhub.model.AIRecommendationRequest;
import com.glowhub.model.AIRecommendationResponse;
import com.glowhub.model.ChatRequest;
import com.glowhub.service.AIAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIAgentService aiAgentService;

    public AIController(AIAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody AIRecommendationRequest request) {
        if (!aiAgentService.isConfigured()) {
            return ResponseEntity.status(503)
                .body(Map.of("detail", "AI service not configured. Set ANTHROPIC_API_KEY."));
        }
        if (request.getProblem() == null || request.getProblem().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("detail", "Problem field is required."));
        }
        try {
            AIRecommendationResponse response = aiAgentService.getRecommendations(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        if (!aiAgentService.isConfigured()) {
            return ResponseEntity.status(503)
                .body(Map.of("detail", "AI service not configured. Set ANTHROPIC_API_KEY."));
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("detail", "Message field is required."));
        }
        try {
            String reply = aiAgentService.chat(request.getMessage(), request.getHistory());
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", e.getMessage()));
        }
    }
}
