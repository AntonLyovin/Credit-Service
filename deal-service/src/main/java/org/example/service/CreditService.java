package org.example.service;

import org.example.model.dto.FinishRegistrationRequestDto;
import org.springframework.http.ResponseEntity;

import javax.naming.ServiceUnavailableException;

public interface CreditService {
    ResponseEntity<Void> finishCalculateCredit(FinishRegistrationRequestDto requestDto, String statementId) throws ServiceUnavailableException;
}
