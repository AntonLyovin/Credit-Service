package org.example.service;

import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.model.StatusHistory;
import org.example.model.entity.Client;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.repository.ClientRepository;
import org.example.repository.StatementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class StatementServiceImp implements StatementService {
    StatementRepository statementRepository;
    ClientRepository clientRepository;
    RestTemplate restTemplate;

    @Autowired
    public void setClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Autowired
    public void setStatementRepository(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final String CALCULATOR_SERVICE_URL = "http://localhost:8080/calculator/offers";

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
        return Statement.builder()
                .clientId(client)
                .statementId(UUID.randomUUID())
                .creationDate(LocalDate.now())
                .signDate(LocalDate.now())
                .statusHistory(createStatusHistory())
                .build();
    }

    private Client convertToClient(LoanStatementRequestDto dto) {
        return Client.builder()
                .clientId(UUID.randomUUID())
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .birthDate(dto.getBirthdate())
                .email(dto.getEmail())
                .gender(null)
                .martialStatus(null)
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

            HttpEntity<LoanStatementRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
            ResponseEntity<List<LoanOfferDto>> response = restTemplate.exchange(
                    CALCULATOR_SERVICE_URL,
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

    private StatusHistory createStatusHistory() {
        return StatusHistory.builder()
                .time(LocalDate.now())
                .status(ApplicationStatus.PREAPPROVAL)
                .changeType(ChangeType.AUTOMATIC)
                .build();
    }

    @Transactional
    public void selectOffer(LoanOfferDto loanOfferDto) {
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


        statement.setAppliedOffer(loanOfferDto);


        statementRepository.save(statement);
    }
}
