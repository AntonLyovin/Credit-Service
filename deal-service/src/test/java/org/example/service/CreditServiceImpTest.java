package org.example.service;

import org.example.config.ScoringServiceProperties;
import org.example.model.PaymentSchedule;
import org.example.model.dto.CreditDto;
import org.example.model.dto.PaymentScheduleElementDto;
import org.example.model.dto.ScoringDataDto;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.CreditStatus;
import org.example.repository.CreditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceImpTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ScoringServiceProperties properties;

    @InjectMocks
    private CreditServiceImp creditService;

    @Test
    void createCredit_SuccessfulCalculation_ShouldSaveAndReturnCredit() throws Exception {
        // Arrange
        ScoringDataDto scoringData = createValidScoringData();
        Statement statement = new Statement();
        CreditDto creditDto = createValidCreditDto();

        when(properties.getUrl()).thenReturn("http://scoring-service/calculate");
        when(restTemplate.postForEntity(anyString(), any(), eq(CreditDto.class)))
                .thenReturn(new ResponseEntity<>(creditDto, HttpStatus.OK));
        when(creditRepository.save(any(Credit.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Credit result = creditService.createCredit(scoringData, statement);

        // Assert
        assertNotNull(result);
        assertEquals(creditDto.getAmount(), result.getAmount());
        assertEquals(creditDto.getTerm(), result.getTerm());
        assertEquals(creditDto.getMonthlyPayment(), result.getMonthlyPayment());
        assertEquals(CreditStatus.CALCULATED, result.getCreditStatus());
        assertEquals(creditDto.getPaymentSchedule().size(), result.getPaymentSchedule().size());

        verify(creditRepository).save(any(Credit.class));
        verify(restTemplate).postForEntity(eq("http://scoring-service/calculate"), eq(scoringData), eq(CreditDto.class));
    }

    @Test
    void createCredit_ScoringServiceError_ShouldThrowServiceUnavailable() {
        // Arrange
        ScoringDataDto scoringData = createValidScoringData();
        Statement statement = new Statement();

        when(properties.getUrl()).thenReturn("http://scoring-service/calculate");
        when(restTemplate.postForEntity(anyString(), any(), eq(CreditDto.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        // Act & Assert
        assertThrows(ServiceUnavailableException.class, () -> {
            creditService.createCredit(scoringData, statement);
        });
    }


    @Test
    void calculateCredit_SuccessfulResponse_ShouldReturnCreditDto() throws Exception {
        // Arrange
        ScoringDataDto scoringData = createValidScoringData();
        CreditDto expectedCreditDto = createValidCreditDto();

        when(properties.getUrl()).thenReturn("http://scoring-service/calculate");
        when(restTemplate.postForEntity(anyString(), any(), eq(CreditDto.class)))
                .thenReturn(new ResponseEntity<>(expectedCreditDto, HttpStatus.OK));

        // Act
        CreditDto result = creditService.calculateCredit(scoringData);

        // Assert
        assertEquals(expectedCreditDto, result);
    }

    @Test
    void buildCreditEntity_ValidDto_ShouldReturnCorrectEntity() {
        // Arrange
        CreditDto creditDto = createValidCreditDto();

        // Act
        Credit result = creditService.buildCreditEntity(creditDto);

        // Assert
        assertNotNull(result.getCreditId());
        assertEquals(creditDto.getAmount(), result.getAmount());
        assertEquals(creditDto.getTerm(), result.getTerm());
        assertEquals(creditDto.getMonthlyPayment(), result.getMonthlyPayment());
        assertEquals(creditDto.getRate(), result.getRate());
        assertEquals(creditDto.getPsk(), result.getPsk());
        assertEquals(creditDto.getIsInsuranceEnabled(), result.getInsuranceEnabled());
        assertEquals(creditDto.getIsSalaryClient(), result.getSalaryClient());
        assertEquals(CreditStatus.CALCULATED, result.getCreditStatus());
        assertEquals(creditDto.getPaymentSchedule().size(), result.getPaymentSchedule().size());
    }

    @Test
    void mapToPaymentSchedule_ValidDto_ShouldReturnCorrectEntity() {
        // Arrange
        PaymentScheduleElementDto dto = new PaymentScheduleElementDto();
        dto.setNumber(1);
        dto.setDate(LocalDate.now());
        dto.setTotalPayment(BigDecimal.valueOf(1000));
        dto.setInterestPayment(BigDecimal.valueOf(100));
        dto.setDebtPayment(BigDecimal.valueOf(900));
        dto.setRemainingDebt(BigDecimal.valueOf(9000));

        // Act
        PaymentSchedule result = creditService.mapToPaymentSchedule(dto);

        // Assert
        assertEquals(dto.getNumber(), result.getNumber());
        assertEquals(dto.getDate(), result.getDate());
        assertEquals(dto.getTotalPayment(), result.getTotalPayment());
        assertEquals(dto.getInterestPayment(), result.getInterestPayment());
        assertEquals(dto.getDebtPayment(), result.getDebtPayment());
        assertEquals(dto.getRemainingDebt(), result.getRemainingDebt());
    }

    private ScoringDataDto createValidScoringData() {
        ScoringDataDto dto = new ScoringDataDto();
        dto.setAmount(BigDecimal.valueOf(100000));
        dto.setTerm(12);
        dto.setFirstName("Ivan");
        dto.setLastName("Ivanov");
        dto.setMiddleName("Ivanovich");
        dto.setBirthdate(LocalDate.of(1990, 1, 1));
        dto.setPassportSeries("1234");
        dto.setPassportNumber("123456");
        return dto;
    }

    private CreditDto createValidCreditDto() {
        PaymentScheduleElementDto scheduleElement = new PaymentScheduleElementDto();
        scheduleElement.setNumber(1);
        scheduleElement.setDate(LocalDate.now());
        scheduleElement.setTotalPayment(BigDecimal.valueOf(1000));
        scheduleElement.setInterestPayment(BigDecimal.valueOf(100));
        scheduleElement.setDebtPayment(BigDecimal.valueOf(900));
        scheduleElement.setRemainingDebt(BigDecimal.valueOf(9000));

        CreditDto dto = new CreditDto();
        dto.setAmount(BigDecimal.valueOf(100000));
        dto.setTerm(12);
        dto.setMonthlyPayment(BigDecimal.valueOf(9000));
        dto.setRate(BigDecimal.valueOf(8.5));
        dto.setPsk(BigDecimal.valueOf(108000));
        dto.setIsInsuranceEnabled(true);
        dto.setIsSalaryClient(true);
        dto.setPaymentSchedule(List.of(scheduleElement));
        return dto;
    }
}