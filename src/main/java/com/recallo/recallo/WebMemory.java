package com.recallo.recallo;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "web_memories")
public class WebMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(384)")
    private float[] embedding;

    // NEW: Timestamp column
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public WebMemory() {}

    public WebMemory(String title, String url, String content, float[] embedding) {
        this.title = title;
        this.url = url;
        this.content = content;
        this.embedding = embedding;
    }

    // NEW: Automatically stamps the exact time right before saving to the database
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getContent() { return content; }
    public float[] getEmbedding() { return embedding; }
    public LocalDateTime getCreatedAt() { return createdAt; } // NEW

    public void setTitle(String title) { this.title = title; }
    public void setUrl(String url) { this.url = url; }
    public void setContent(String content) { this.content = content; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; } // NEW
}