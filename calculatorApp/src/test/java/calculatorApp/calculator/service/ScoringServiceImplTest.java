package calculatorApp.calculator.service;

import calculatorApp.calculator.model.dto.CreditDto;
import calculatorApp.calculator.model.dto.EmploymentDto;
import calculatorApp.calculator.model.dto.PaymentScheduleElementDto;
import calculatorApp.calculator.model.dto.ScoringDataDto;
import calculatorApp.calculator.model.enumerated.EmploymentStatusEnum;
import calculatorApp.calculator.model.enumerated.Gender;
import calculatorApp.calculator.model.enumerated.MartialStatus;
import calculatorApp.calculator.model.enumerated.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ScoringServiceImplTest {

    @InjectMocks
    private ScoringServiceImpl scoringService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ScoringDataDto createValidData() {
        EmploymentDto employment = new EmploymentDto();
        employment.setWorkExperienceTotal(36);
        employment.setGetWorkExperienceCurrent(12);
        employment.setSalary(BigDecimal.valueOf(100000));
        employment.setEmploymentStatus(EmploymentStatusEnum.SELF_EMPLOYED);
        employment.setPosition(Position.MIDDLE_MANAGER);

        ScoringDataDto data = new ScoringDataDto();
        data.setAmount(BigDecimal.valueOf(200000));
        data.setTerm(12);
        data.setFirstName("Ant");
        data.setLastName("Ant");
        data.setMiddleName("Ant");
        data.setGender(Gender.FEMALE);
        data.setBirthdate(LocalDate.of(1985, 1, 1));
        data.setPassportSeries("4444");
        data.setPassportNumber("555555");
        data.setPassportIssueDate(LocalDate.of(1995, 1, 1));
        data.setPassportIssueBranch("dad");
        data.setMaritalStatus(MartialStatus.MARRIED);
        data.setDependentAmount(144444);
        data.setEmployment(employment);
        data.setIsInsuranceEnabled(true);
        data.setIsSalaryClient(true);                  // еще один флаг (например, согласие на обработку данных)


        return data;
    }

    @Test
    void testCalculateCredit_SuccessfulCalculation() {

        ScoringDataDto data = createValidData();

        CreditDto credit = scoringService.calculateCredit(data);

        assertNotNull(credit);
        assertEquals(data.getAmount(), credit.getAmount());
        assertEquals(data.getTerm(), credit.getTerm());
        assertNotNull(credit.getRate());
        assertNotNull(credit.getPsk());
        List<PaymentScheduleElementDto> schedule = credit.getPaymentSchedule();
        assertNotNull(schedule);
        assertFalse(schedule.isEmpty());

        PaymentScheduleElementDto lastPayment = schedule.get(schedule.size() - 1);
        BigDecimal remainingDebt = lastPayment.getRemainingDebt();
        assertTrue(remainingDebt.compareTo(BigDecimal.ZERO) >= 0);
    }

}




