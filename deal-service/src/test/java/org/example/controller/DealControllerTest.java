package org.example.controller;

import org.example.exception.custom.ScoringServiceUnavailableException;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.service.CreditService;
import org.example.service.StatementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.ServiceUnavailableException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {

    @Mock
    private StatementService statementService;

    @Mock
    private CreditService creditService;

    @InjectMocks
    private DealController dealController;

    @Test
    void createStatement_ShouldReturnLoanOffers() throws ServiceUnavailableException {
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto();
        List<LoanOfferDto> expectedOffers = List.of(new LoanOfferDto());
        when(statementService.createStatement(any(LoanStatementRequestDto.class))).thenReturn(expectedOffers);

        List<LoanOfferDto> result = dealController.createStatement(requestDto);

        assertEquals(expectedOffers, result);
        verify(statementService).createStatement(requestDto);
    }

    @Test
    void selectOffer_ShouldReturnOk() {
        LoanOfferDto offerDto = new LoanOfferDto();

        ResponseEntity<Void> response = dealController.selectOffer(offerDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(statementService).selectOffer(offerDto);
    }

    @Test
    void finishCalculateCredit_ShouldReturnOk() throws ServiceUnavailableException {
        String statementId = "550e8400-e29b-41d4-a716-446655440000";
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        when(creditService.finishCalculateCredit(any(), anyString()))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Void> response = dealController.finishCalculateCredit(statementId, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(creditService).finishCalculateCredit(requestDto, statementId);
    }

    @Test
    void finishCalculateCredit_WhenServiceUnavailable_ShouldThrowException() throws ServiceUnavailableException {
        String statementId = "550e8400-e29b-41d4-a716-446655440000";
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();

        when(creditService.finishCalculateCredit(any(FinishRegistrationRequestDto.class), eq(statementId)))
                .thenThrow(new ServiceUnavailableException("Scoring service unavailable"));

        assertThrows(ServiceUnavailableException.class, () -> {
            dealController.finishCalculateCredit(statementId, requestDto);
        });

        verify(creditService).finishCalculateCredit(requestDto, statementId);
    }

    @Test
    void finishCalculateCredit_WhenInvalidStatementId_ShouldPassThrough() throws ServiceUnavailableException {
        String invalidStatementId = "invalid-uuid";
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();

        when(creditService.finishCalculateCredit(any(), any())).thenReturn(null);

        ResponseEntity<Void> response = dealController.finishCalculateCredit(invalidStatementId, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(creditService).finishCalculateCredit(requestDto, invalidStatementId);
    }
}