package org.example.service;

import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.springframework.http.ResponseEntity;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.UUID;

public interface GatewayService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException;
    ResponseEntity<Void> selectOffer(LoanOfferDto offerDto) throws ServiceUnavailableException;
    ResponseEntity<Void> processCreditCalculation(FinishRegistrationRequestDto requestDto, UUID statementId) throws ServiceUnavailableException;
    ResponseEntity<Void> processSendDocuments(UUID statementId) throws ServiceUnavailableException;
    ResponseEntity<Void> processSignDocuments( UUID statementId) throws ServiceUnavailableException;
    ResponseEntity<Void> processVerifySesCode( UUID statementId, String sesCode) throws ServiceUnavailableException;
}
