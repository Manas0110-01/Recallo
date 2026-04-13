package com.recallo.recallo;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    // This loads the mini AI model directly onto your computer's memory
    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    public float[] generateMemoryVector(String textToRemember) {
        System.out.println("AI is reading the text and converting it to math...");
        
        // The AI generates the embedding (the array of numbers)
        Embedding embedding = embeddingModel.embed(textToRemember).content();
        
        // Return the actual array of decimal numbers
        return embedding.vector();
    }
}