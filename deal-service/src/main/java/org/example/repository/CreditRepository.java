package org.example.repository;

import org.example.model.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditRepository extends JpaRepository<Credit, UUID> {
    Optional<Credit> findById(UUID creditId);
}
