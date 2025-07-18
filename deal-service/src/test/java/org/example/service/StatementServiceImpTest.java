package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.model.StatusHistory;
import org.example.model.dto.LoanOfferDto;
import org.example.model.entity.Client;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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