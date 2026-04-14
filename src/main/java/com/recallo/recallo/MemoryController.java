package com.recallo.recallo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/memory")
public class MemoryController {

    @Autowired
    private WebMemoryRepository webMemoryRepository;

    @Autowired
    private AiService aiService;

    @PostMapping("/save")
    public String saveMemory(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String url = payload.get("url");
        String fullContent = payload.get("content"); 

        if (fullContent == null || fullContent.isEmpty()) {
            fullContent = "No readable content found.";
        }

        System.out.println("--- NEW SAVE INITIATED ---");
        System.out.println("Scraped Content Length: " + fullContent.length() + " characters");

        // 1. The Sliding Window Chunking Algorithm
        int chunkSize = 1500; // Safe character limit for the AI model
        int overlap = 300;    // Overlap to keep sentences flowing contextually
        
        List<WebMemory> chunksToSave = new ArrayList<>();

        for (int i = 0; i < fullContent.length(); i += (chunkSize - overlap)) {
            // Determine where this chunk ends
            int end = Math.min(fullContent.length(), i + chunkSize);
            String chunkText = fullContent.substring(i, end);

            // 2. Combine the title and this specific chunk
            String textForAi = "Website Title: " + title + "\nURL: " + url + "\nContent Chunk: " + chunkText;

            // 3. Generate a math vector for JUST this chunk
            float[] aiBrainNumbers = aiService.generateMemoryVector(textForAi);

            // 4. Create a database record for this chunk
            WebMemory memoryChunk = new WebMemory(title, url, chunkText, aiBrainNumbers);
            chunksToSave.add(memoryChunk);

            // Stop the loop if we have reached the end of the text
            if (end == fullContent.length()) {
                break;
            }
        }

        // 5. Batch save all chunks to the database at once!
        webMemoryRepository.saveAll(chunksToSave);

        System.out.println("Successfully sliced and saved " + chunksToSave.size() + " chunks to the database!");
        System.out.println("--------------------------");

        return "Memory and " + chunksToSave.size() + " AI Vectors permanently saved!";
    }

    @PostMapping("/search")
    public List<WebMemory> searchMemory(@RequestBody Map<String, String> payload) {
        String userQuestion = payload.get("query");

        System.out.println("--- NEW SEARCH INITIATED ---");
        System.out.println("User asked: " + userQuestion);

        // 1. Convert the user's question into math
        float[] searchVector = aiService.generateMemoryVector(userQuestion);

        // 2. Ask the database to find the 3 closest chunk matches
        List<WebMemory> results = webMemoryRepository.searchSimilarMemories(searchVector);

        System.out.println("Found " + results.size() + " matches!");
        System.out.println("----------------------------");

        // 3. Return the matching chunks back to the user
        return results;
    }

    @GetMapping("/all")
    public List<WebMemory> getAllMemories() {
        System.out.println("--- DASHBOARD REQUESTED ALL MEMORIES ---");
        // webMemoryRepository.findAll() automatically writes the SQL to get everything
        return webMemoryRepository.findAll();
    }

    // NEW: The Delete Endpoint
    @DeleteMapping("/delete/{id}")
    public String deleteMemory(@PathVariable Long id) {
        System.out.println("--- DELETING MEMORY CHUNK ID: " + id + " ---");
        // Spring Data JPA handles the SQL deletion automatically
        webMemoryRepository.deleteById(id);
        return "Memory chunk deleted successfully!";
    }
}