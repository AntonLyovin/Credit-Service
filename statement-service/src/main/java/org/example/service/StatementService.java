package org.example.service;

import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.springframework.http.ResponseEntity;

import javax.naming.ServiceUnavailableException;
import java.util.List;

public interface StatementService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException;
    ResponseEntity<Void> selectOffer(LoanOfferDto offerDto) throws ServiceUnavailableException;
}
