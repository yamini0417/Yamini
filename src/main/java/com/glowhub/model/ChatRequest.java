package com.glowhub.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatRequest {
    private String message;
    private List<ChatMessage> history;
}
