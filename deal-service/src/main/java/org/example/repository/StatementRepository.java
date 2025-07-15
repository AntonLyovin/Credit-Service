package org.example.repository;

import org.example.model.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface StatementRepository extends JpaRepository<Statement, UUID> {
    Optional<Statement> findById(UUID statementId);
}
