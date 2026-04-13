package com.recallo.recallo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

        // 1. Combine the text we want the AI to understand
        String textForAi = "Website Title: " + title + " URL: " + url;

        // 2. Feed it to the AI and get the numbers back!
        float[] aiBrainNumbers = aiService.generateMemoryVector(textForAi);

        System.out.println("--- AI PROCESSING COMPLETE ---");
        System.out.println("The AI generated exactly " + aiBrainNumbers.length + " numbers.");

        // 3. Save the math array DIRECTLY to the database!
        WebMemory newMemory = new WebMemory(title, url, aiBrainNumbers);
        webMemoryRepository.save(newMemory);

        return "Memory and AI Vectors permanently saved!";
    }
    @PostMapping("/search")
    public List<WebMemory> searchMemory(@RequestBody Map<String, String> payload) {
        String userQuestion = payload.get("query");

        System.out.println("--- NEW SEARCH INITIATED ---");
        System.out.println("User asked: " + userQuestion);

        // 1. Convert the user's question into 384 numbers using the same AI model
        float[] searchVector = aiService.generateMemoryVector(userQuestion);

        // 2. Ask the database to find the 3 closest matches
        List<WebMemory> results = webMemoryRepository.searchSimilarMemories(searchVector);

        System.out.println("Found " + results.size() + " matches!");
        System.out.println("----------------------------");

        // 3. Return the matching websites back to the user
        return results;
    }
}