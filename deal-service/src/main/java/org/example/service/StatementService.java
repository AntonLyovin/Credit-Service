package org.example.service;



import org.example.model.StatusHistory;
import org.example.model.dto.LoanOfferDto;
import org.example.model.entity.Client;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;

import java.util.List;
import java.util.UUID;

public interface StatementService {
    Statement createStatement(Client client);
    void applyOfferToStatement(UUID statementId, LoanOfferDto offerDto);
    Statement getStatementById(UUID statementId);
    void updateStatementWithCredit(Statement statement, Credit credit);
    List<StatusHistory> createInitialStatusHistory();
}
