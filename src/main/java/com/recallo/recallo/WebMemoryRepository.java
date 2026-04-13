package com.recallo.recallo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebMemoryRepository extends JpaRepository<WebMemory, Long> {
}