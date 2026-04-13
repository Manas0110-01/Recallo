package com.recallo.recallo;
import jakarta.persistence.*;

@Entity
@Table(name = "web_memories")
public class WebMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String url;

    // Empty constructor needed by Spring Boot
    public WebMemory() {}

    public WebMemory(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setUrl(String url) { this.url = url; }
}