package com.recallo.recallo;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "web_memories")
public class WebMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String url;

    // NEW: A massive text column to hold the scraped website content!
    @Column(columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(384)")
    private float[] embedding;

    public WebMemory() {}

    // UPDATED: Added content to the constructor
    public WebMemory(String title, String url, String content, float[] embedding) {
        this.title = title;
        this.url = url;
        this.content = content;
        this.embedding = embedding;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getContent() { return content; }
    public float[] getEmbedding() { return embedding; }

    public void setTitle(String title) { this.title = title; }
    public void setUrl(String url) { this.url = url; }
    public void setContent(String content) { this.content = content; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}