package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.config.PreScoringServiceProperties;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.entity.Client;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.repository.ClientRepository;
import org.example.repository.StatementRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class StatementServiceImp implements StatementService {
    private final StatementRepository statementRepository;
    private final ClientRepository clientRepository;
    private final RestTemplate restTemplate;
    private final PreScoringServiceProperties preScoringServiceProperties;

    public StatementServiceImp(StatementRepository statementRepository, ClientRepository clientRepository, RestTemplate restTemplate, PreScoringServiceProperties preScoringServiceProperties) {
        this.statementRepository = statementRepository;
        this.clientRepository = clientRepository;
        this.restTemplate = restTemplate;
        this.preScoringServiceProperties = preScoringServiceProperties;
    }


    public List<LoanOfferDto> createStatement(LoanStatementRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("Запрос на выписку по кредиту не может быть пустым");
        }


        Client client = convertToClient(requestDto);
        client = clientRepository.save(client);
        log.info("Создание клиента с ID: {}", client.getClientId());


        Statement statement = createNewStatement(client);
        statement = statementRepository.save(statement);
        log.info("Создание нового statement ID: {}", statement.getStatementId());

        List<LoanOfferDto> offers = fetchOffersFromCalculator(requestDto);
        UUID statementId = statement.getStatementId();
        offers.forEach(offer -> offer.setStatementId(statementId));
        offers.sort(Comparator.comparing(LoanOfferDto::getRate));

        log.info("ВОзвращение {} предложений для statement ID: {}", offers.size(), statement.getStatementId());
        return offers;
    }

    private Statement createNewStatement(Client client) {
        log.info("Создание предложения предложений для клиента с ID: {}", client.getClientId());
        return Statement.builder()
                .clientId(client)
                .statementId(UUID.randomUUID())
                .creationDate(LocalDate.now())
                .signDate(LocalDate.now())
                .statusHistory(createStatusHistory())
                .build();
    }

    Client convertToClient(LoanStatementRequestDto dto) {
        log.info("Начало преобразования в клиента из ДТО");
        return Client.builder()
                .clientId(UUID.randomUUID())
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .birthDate(dto.getBirthdate())
                .email(dto.getEmail())
                .gender(null)
                .maritalStatus(null)
                .dependentAmount(null)
                .passport(null)
                .employment(null)
                .accountNumber(null)
                .build();
    }

    private List<LoanOfferDto> fetchOffersFromCalculator(LoanStatementRequestDto requestDto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            log.info("Отправка данных для расчета в калькулятор");
            HttpEntity<LoanStatementRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
            ResponseEntity<List<LoanOfferDto>> response = restTemplate.exchange(
                    preScoringServiceProperties.getUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ServiceUnavailableException("Не удалось получить ответ от службы калькулятора");
            }

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Ошибка с отправкой предложений в калькулятор", e);
            throw new ResourceAccessException("Калькулятор недоступен " + e.getMessage());
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    List<StatusHistory> createStatusHistory() {
        log.info("Создание statusHistory");
        List<StatusHistory> statusHistoryList = new ArrayList<>();
        statusHistoryList.add(StatusHistory.builder()
                .time(LocalDate.now())
                .status(ApplicationStatus.PREAPPROVAL)
                .changeType(ChangeType.AUTOMATIC)
                .build());
        return statusHistoryList;

    }

    @Transactional
    public void selectOffer(LoanOfferDto loanOfferDto) {
        log.info("Начало выбора предложения с ID {}",  loanOfferDto.getStatementId());
        Statement statement = statementRepository.findById(loanOfferDto.getStatementId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Предложение с таким Id не найдено " + loanOfferDto.getStatementId()));

        statement.setStatus(ApplicationStatus.APPROVED);

        StatusHistory newStatusHistory = StatusHistory.builder()
                .status(ApplicationStatus.APPROVED)
                .time(LocalDate.now())
                .changeType(ChangeType.MANUAL)
                .build();

        List<StatusHistory> statusHistory = statement.getStatusHistory();
        statusHistory.add(newStatusHistory);
        statement.setStatusHistory(statusHistory);

        AppliedOffer appliedOffer = convertToAppliedOffer(loanOfferDto);

        if (statement.getAppliedOffer() != null) {
            AppliedOffer existingOffer = statement.getAppliedOffer();
            updateAppliedOffer(existingOffer, appliedOffer);
        } else {
            statement.setAppliedOffer(appliedOffer);
        }

        statementRepository.save(statement);
    }

    private void updateAppliedOffer(AppliedOffer existing, AppliedOffer newData) {
        log.info("Обновление предложения:");
        existing.setRequestedAmount(newData.getRequestedAmount());
        existing.setTotalAmount(newData.getTotalAmount());
        existing.setTerm(newData.getTerm());
        existing.setMonthlyPayment(newData.getMonthlyPayment());
        existing.setRate(newData.getRate());
        existing.setIsInsuranceEnabled(newData.getIsInsuranceEnabled());
        existing.setIsSalaryClient(newData.getIsSalaryClient());
    }

    public AppliedOffer convertToAppliedOffer(LoanOfferDto dto) {
        log.info("Конвертация из LoanOfferDto в AppliedOffer");
        return AppliedOffer.builder()
                .statementId(UUID.randomUUID())
                .statementId(dto.getStatementId())
                .requestedAmount(dto.getRequestedAmount())
                .totalAmount(dto.getTotalAmount())
                .term(dto.getTerm())
                .monthlyPayment(dto.getMonthlyPayment())
                .rate(dto.getRate())
                .isInsuranceEnabled(dto.getIsInsuranceEnabled())
                .isSalaryClient(dto.getIsSalaryClient())
                .build();
    }

}
