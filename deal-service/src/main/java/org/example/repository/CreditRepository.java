package org.example.repository;

import org.example.model.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface CreditRepository extends JpaRepository<Credit, UUID> {
    Optional<Credit> findById(UUID creditId);
}
