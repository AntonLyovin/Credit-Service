package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.config.PreScoringServiceProperties;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.entity.Client;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.repository.ClientRepository;
import org.example.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementServiceImpTest {

    @Mock
    private StatementRepository statementRepository;

    @InjectMocks
    private StatementServiceImp statementService;

    private Client testClient;
    private LoanOfferDto testOffer;
    private UUID testStatementId;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(UUID.randomUUID());

        testOffer = new LoanOfferDto(
                UUID.randomUUID(),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(90000),
                12,
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(9500),
                true,
                true
        );

        testStatementId = UUID.randomUUID();
    }


    @Test
    void applyOfferToStatement_WhenStatementNotFound_ShouldThrowException() {
        // Arrange
        when(statementRepository.findById(testStatementId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            statementService.applyOfferToStatement(testStatementId, testOffer);
        });
    }

    @Test
    void getStatementById_ShouldReturnStatement() {
        // Arrange
        Statement expectedStatement = Statement.builder()
                .statementId(testStatementId)
                .build();

        when(statementRepository.findById(testStatementId)).thenReturn(Optional.of(expectedStatement));

        // Act
        Statement result = statementService.getStatementById(testStatementId);

        // Assert
        assertEquals(expectedStatement, result);
        verify(statementRepository).findById(testStatementId);
    }

    @Test
    void getStatementById_WhenStatementNotFound_ShouldThrowException() {
        // Arrange
        when(statementRepository.findById(testStatementId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            statementService.getStatementById(testStatementId);
        });
    }

    @Test
    void createInitialStatusHistory_ShouldReturnCorrectHistory() {
        // Act
        List<StatusHistory> result = statementService.createInitialStatusHistory();

        // Assert
        assertEquals(1, result.size());
        assertEquals(ApplicationStatus.PREAPPROVAL, result.get(0).getStatus());
        assertEquals(ChangeType.AUTOMATIC, result.get(0).getChangeType());
        assertEquals(LocalDate.now(), result.get(0).getTime());
    }

}