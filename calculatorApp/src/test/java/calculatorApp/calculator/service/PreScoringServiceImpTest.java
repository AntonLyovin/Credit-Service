package calculatorApp.calculator.service;

import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PreScoringServiceImpTest {
    @InjectMocks
    private PreScoringServiceImpl preScoringService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        preScoringService = new PreScoringServiceImpl();
    }
    private LoanStatementRequestDto createRequestDto(){
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto();
        requestDto.setAmount( BigDecimal.valueOf(100000));
        requestDto.setTerm(12);
        requestDto.setFirstName("Иван");
        requestDto.setLastName("Иванов");
        requestDto.setMiddleName("Иванович");
        requestDto.setEmail("test@example.com");
        requestDto.setBirthdate(LocalDate.of(1990, 1, 1));
        requestDto.setPassportSeries("4444");
        requestDto.setPassportNumber("123456");
        return  requestDto;
    }

    @Test
    void calculatePreOffer_ShouldReturnSortedLoanOffers() {
        LoanStatementRequestDto data = createRequestDto();


        List<LoanOfferDto> offers = preScoringService.calculatePreOffer(data);

        boolean someCondition = offers.stream().sorted().equals(offers);
        assertFalse(someCondition);

        for (int i = 0; i < offers.size() - 1; i++) {
            assertTrue(offers.get(i).getRate().compareTo(offers.get(i + 1).getRate()) <= 0);
        }

        for (LoanOfferDto offer : offers) {
            assertNotNull(offer.getRequestedAmount());
            assertNotNull(offer.getTotalAmount());
            assertNotNull(offer.getRate());
            assertNotNull(offer.getMonthlyPayment());
            assertTrue(offer.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(offer.getTerm() > 0);
        }
    }

    @Test
    void testCalculatePreOffer_ReturnsCorrectOffers() {
        LoanStatementRequestDto data = createRequestDto();


        List<LoanOfferDto> offers;
        offers = preScoringService.calculatePreOffer(data);

        assertNotNull(offers);
        assertEquals(4, offers.size());

        for (int i = 0; i < offers.size() - 1; i++) {
            assertTrue(offers.get(i).getRate().compareTo(offers.get(i + 1).getRate()) <= 0);
        }

        for (LoanOfferDto offer : offers) {
            assertNotNull(offer.getRequestedAmount());
            assertNotNull(offer.getTotalAmount());
            assertNotNull(offer.getRate());
            assertNotNull(offer.getMonthlyPayment());
            assertTrue(offer.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(offer.getTotalAmount().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(offer.getMonthlyPayment().compareTo(BigDecimal.ZERO) > 0);
        }
    }

}
