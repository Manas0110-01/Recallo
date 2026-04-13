package com.recallo.recallo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebMemoryRepository extends JpaRepository<WebMemory, Long> {

    // This native SQL query tells Supabase to find the 3 closest matching vectors!
    @Query(value = "SELECT * FROM web_memories ORDER BY embedding <=> cast(:queryVector as vector) LIMIT 3", nativeQuery = true)
    List<WebMemory> searchSimilarMemories(@Param("queryVector") float[] queryVector);
}