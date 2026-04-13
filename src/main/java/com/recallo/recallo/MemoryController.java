package com.recallo.recallo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
}