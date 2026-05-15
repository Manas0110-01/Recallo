package com.recallo.recallo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

@Service
public class AiService {

    private final EmbeddingModel embeddingModel;
    private final StreamingChatLanguageModel streamingChatModel;

    // We use @Value to securely load the key from application.properties
    public AiService(@Value("${groq.api.key}") String groqApiKey) {
        
        // The local embedding model (Runs on your laptop, 0 API limits!)
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // The Ferrari Engine: Groq Llama 3.1 (Lightning Fast!)
        this.streamingChatModel = OpenAiStreamingChatModel.builder()
                .apiKey(groqApiKey) // <--- SECURE: Loaded dynamically
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.1-8b-instant")
                .temperature(0.3)
                .build();
    }

    public float[] generateMemoryVector(String text) {
        return embeddingModel.embed(text).content().vector();
    }

    public StreamingChatLanguageModel getStreamingChatModel() {
        return streamingChatModel;
    }
}