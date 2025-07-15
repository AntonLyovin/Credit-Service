package org.example.service;

import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.entity.Client;

public interface ClientService {
    Client createClient(LoanStatementRequestDto requestDto);
}
