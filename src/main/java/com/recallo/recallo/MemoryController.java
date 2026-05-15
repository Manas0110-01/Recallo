package com.recallo.recallo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/memory")
public class MemoryController {

    @Autowired
    private WebMemoryRepository webMemoryRepository;

    @Autowired
    private AiService aiService;

    public static class ChatMessage {
        private String role;
        private String text;
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class AskRequest {
        private String query;
        private Integer days;
        private List<ChatMessage> history;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public Integer getDays() { return days; }
        public void setDays(Integer days) { this.days = days; }
        public List<ChatMessage> getHistory() { return history; }
        public void setHistory(List<ChatMessage> history) { this.history = history; }
    }

    @PostMapping("/save")
    public String saveMemory(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String url = payload.get("url");
        String fullContent = payload.get("content");

        if (fullContent == null || fullContent.isEmpty()) {
            fullContent = "No readable content found.";
        }

        int chunkSize = 1500;
        int overlap = 300;
        List<WebMemory> chunksToSave = new ArrayList<>();

        for (int i = 0; i < fullContent.length(); i += (chunkSize - overlap)) {
            int end = Math.min(fullContent.length(), i + chunkSize);
            String chunkText = fullContent.substring(i, end);
            String textForAi = "Website Title: " + title + "\nURL: " + url + "\nContent Chunk: " + chunkText;
            float[] aiBrainNumbers = aiService.generateMemoryVector(textForAi);
            WebMemory memoryChunk = new WebMemory(title, url, chunkText, aiBrainNumbers);
            chunksToSave.add(memoryChunk);
            if (end == fullContent.length()) break;
        }

        webMemoryRepository.saveAll(chunksToSave);
        return "Memory and " + chunksToSave.size() + " AI Vectors permanently saved!";
    }

    @PostMapping("/search")
    public List<WebMemory> searchMemory(@RequestBody Map<String, String> payload) {
        String userQuestion = payload.get("query");
        float[] searchVector = aiService.generateMemoryVector(userQuestion);
        return webMemoryRepository.searchSimilarMemories(searchVector);
    }

    @GetMapping("/all")
    public List<WebMemory> getAllMemories() {
        return webMemoryRepository.findAll();
    }

    @DeleteMapping("/delete/{id}")
    public String deleteMemory(@PathVariable Long id) {
        webMemoryRepository.deleteById(id);
        return "Memory chunk deleted successfully!";
    }

    @PostMapping("/ask")
    public ResponseBodyEmitter askAiBrainStream(@RequestBody AskRequest request) {
        String userQuestion = request.getQuery();
        Integer daysLimit = request.getDays();

        float[] searchVector = aiService.generateMemoryVector(userQuestion);

        List<WebMemory> relevantChunks;
        if (daysLimit != null && daysLimit > 0) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysLimit);
            relevantChunks = webMemoryRepository.searchSimilarMemoriesWithTimeFilter(searchVector, cutoffDate);
        } else {
            relevantChunks = webMemoryRepository.searchSimilarMemories(searchVector);
        }

        // 1. TOKEN PROTECTOR: Strictly limit the database memories to 3 chunks maximum
        StringBuilder contextBuilder = new StringBuilder();
        int chunkCount = 0;
        for (WebMemory chunk : relevantChunks) {
            if (chunkCount >= 3) break; // Hard cap
            contextBuilder.append("Source: ").append(chunk.getTitle()).append("\n");
            if (chunk.getCreatedAt() != null) {
                contextBuilder.append("Date Saved: ").append(chunk.getCreatedAt().toLocalDate().toString()).append("\n");
            }
            contextBuilder.append("Content: ").append(chunk.getContent()).append("\n\n");
            chunkCount++;
        }

        // 2. SLIDING WINDOW MEMORY: Only grab the last 4 chat messages (2 questions, 2 answers)
        StringBuilder historyBuilder = new StringBuilder();
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            List<ChatMessage> history = request.getHistory();
            
            // Math to only grab the end of the list
            int startIndex = Math.max(0, history.size() - 4); 
            
            for (int i = startIndex; i < history.size(); i++) {
                ChatMessage msg = history.get(i);
                
                // Extra safety: If an old AI answer was massive, trim it down
                String text = msg.getText();
                if (text.length() > 800) {
                    text = text.substring(0, 800) + "... [Truncated for memory limits]";
                }
                
                historyBuilder.append(msg.getRole().toUpperCase()).append(": ").append(text).append("\n\n");
            }
        }

        String finalPrompt = "You are 'Recallo', an AI second brain. Answer the user's question based strictly on the provided memories below. "
                + "If the user asks a follow-up question, look at the PREVIOUS CONVERSATION to understand the context. "
                + "If the user asks about dates or times they read/visited something, use the 'Date Saved' information provided in the memories. "
                + "If the answer is not in the memories, say 'I don't have a memory of that.' Do not use outside knowledge.\n\n"
                + "USER MEMORIES:\n" + contextBuilder.toString()
                + "PREVIOUS CONVERSATION:\n" + historyBuilder.toString()
                + "CURRENT QUESTION: " + userQuestion;

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(120000L);

        aiService.getStreamingChatModel().generate(finalPrompt, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try { emitter.send(token); } catch (Exception e) { emitter.completeWithError(e); }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                try {
                    System.out.println("AI CRASHED: " + error.getMessage());
                    emitter.send("\n\n ⚠️ [AI Failed: " + error.getMessage() + "]");
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(error);
                }
            }
        });

        return emitter;
    }
}