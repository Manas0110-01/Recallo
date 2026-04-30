package com.recallo.recallo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Service
public class AiService {

    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatModel; // NEW: The conversational brain

    // Pulls your key from application.properties
    public AiService(@Value("${gemini.api.key}") String geminiApiKey) {
        
        // 1. The Math Engine (converts text to vectors)
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // 2. The Conversational Engine (Gemini 2.5 Flash)
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.3) // Keeps answers highly factual and focused
                .build();
    }

    // Existing method: Turns chunks into numbers
    public float[] generateMemoryVector(String text) {
        return embeddingModel.embed(text).content().vector();
    }

    // NEW method: Sends a prompt to Gemini with a protective shield
    public String askGemini(String prompt) {
        try {
            // Try to ask Gemini the question
            return chatModel.generate(prompt);
        } catch (Exception e) {
            // If Google throws a traffic or quota error, catch it gracefully
            if (e.getMessage() != null && (e.getMessage().contains("503") || e.getMessage().contains("429"))) {
                return "⚠️ **Traffic Jam!** Google's AI servers are busy right now (Free Tier limitation). Please wait 10 seconds and click Recall again.";
            }
            // If it's a different error, return the actual error message
            return "⚠️ **AI Error:** " + e.getMessage();
        }
    }
} 