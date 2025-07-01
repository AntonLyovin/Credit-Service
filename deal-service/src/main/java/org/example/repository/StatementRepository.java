package org.example.repository;

import org.example.model.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, UUID> {
    Optional<Statement> findById(UUID statementId);
}
