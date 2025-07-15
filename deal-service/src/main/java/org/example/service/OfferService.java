package org.example.service;

import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;

import javax.naming.ServiceUnavailableException;
import java.util.List;

public interface OfferService {
    List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException;
}
