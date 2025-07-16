package org.example.service;

import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;

import javax.naming.ServiceUnavailableException;
import java.util.List;

public interface StatementService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException;
    void selectOffer(LoanOfferDto offerDto) throws ServiceUnavailableException;
}
