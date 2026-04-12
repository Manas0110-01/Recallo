package com.recallo.recallo;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // This allows your Chrome Extension to talk to this server
@RequestMapping("/api/memory")
public class MemoryController {

    @PostMapping("/save")
    public String saveMemory(@RequestBody Map<String, String> payload) {
        // Extract the data sent by the extension
        String title = payload.get("title");
        String url = payload.get("url");

        // For right now, just print it to the VS Code terminal so we know it arrived
        System.out.println("--- NEW MEMORY RECEIVED ---");
        System.out.println("Title: " + title);
        System.out.println("URL: " + url);
        System.out.println("---------------------------");

        return "Memory received by Spring Boot!";
    }
}