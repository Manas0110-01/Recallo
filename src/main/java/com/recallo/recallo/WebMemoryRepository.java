package com.recallo.recallo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WebMemoryRepository extends JpaRepository<WebMemory, Long> {

    // Original Query: Searches all time
    @Query(value = "SELECT * FROM web_memories ORDER BY embedding <=> cast(:queryVector as vector) LIMIT 3", nativeQuery = true)
    List<WebMemory> searchSimilarMemories(@Param("queryVector") float[] queryVector);

    // NEW Query: Searches ONLY memories newer than the provided "since" date
    @Query(value = "SELECT * FROM web_memories WHERE (created_at IS NULL OR created_at >= cast(:since as timestamp)) ORDER BY embedding <=> cast(:queryVector as vector) LIMIT 3", nativeQuery = true)
    List<WebMemory> searchSimilarMemoriesWithTimeFilter(@Param("queryVector") float[] queryVector, @Param("since") LocalDateTime since);
}