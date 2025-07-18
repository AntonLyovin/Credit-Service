package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.ScoringServiceProperties;
import org.example.model.PaymentSchedule;
import org.example.model.dto.CreditDto;
import org.example.model.dto.PaymentScheduleElementDto;
import org.example.model.dto.ScoringDataDto;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.CreditStatus;
import org.example.repository.CreditRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CreditServiceImp implements CreditService {
    private final CreditRepository creditRepository;
    private final RestTemplate restTemplate;
    private final ScoringServiceProperties properties;

    public CreditServiceImp(CreditRepository creditRepository,
                             RestTemplate restTemplate,
                             ScoringServiceProperties properties) {
        this.creditRepository = creditRepository;
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    @Transactional
    public Credit createCredit(ScoringDataDto scoringData, Statement statement) throws ServiceUnavailableException {
        CreditDto creditDto = calculateCredit(scoringData);
        Credit credit = buildCreditEntity(creditDto);
        return creditRepository.save(credit);
    }

    CreditDto calculateCredit(ScoringDataDto scoringData) throws ServiceUnavailableException {
        try {
            ResponseEntity<CreditDto> response = restTemplate.postForEntity(
                    properties.getUrl(),
                    scoringData,
                    CreditDto.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ServiceUnavailableException("Ошибка сервиса скоринга");
            }

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Ошибка сервиса скоринга", e);
            throw new ServiceUnavailableException("Сервис скоринга недоступен");
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    Credit buildCreditEntity(CreditDto creditDto) {
        List<PaymentSchedule> paymentSchedules = creditDto.getPaymentSchedule().stream()
                .map(this::mapToPaymentSchedule)
                .collect(Collectors.toList());

        return Credit.builder()
                .amount(creditDto.getAmount())
                .term(creditDto.getTerm())
                .monthlyPayment(creditDto.getMonthlyPayment())
                .rate(creditDto.getRate())
                .psk(creditDto.getPsk())
                .paymentSchedule(paymentSchedules)
                .insuranceEnabled(creditDto.getIsInsuranceEnabled())
                .salaryClient(creditDto.getIsSalaryClient())
                .creditStatus(CreditStatus.CALCULATED)
                .build();
    }

    PaymentSchedule mapToPaymentSchedule(PaymentScheduleElementDto dto) {
        return PaymentSchedule.builder()
                .number(dto.getNumber())
                .date(dto.getDate())
                .totalPayment(dto.getTotalPayment())
                .interestPayment(dto.getInterestPayment())
                .debtPayment(dto.getDebtPayment())
                .remainingDebt(dto.getRemainingDebt())
                .build();
    }

}
