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

    @PostMapping("/save")
    public String saveMemory(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String url = payload.get("url");

        // Create a new memory object
        WebMemory newMemory = new WebMemory(title, url);
        
        // Save it to the Supabase database
        webMemoryRepository.save(newMemory);

        System.out.println("Saved to Supabase DB: " + title);
        return "Memory permanently saved!";
    }
}