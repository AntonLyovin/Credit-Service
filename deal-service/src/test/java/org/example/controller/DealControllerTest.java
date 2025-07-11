package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.enumerated.EmploymentStatusEnum;
import org.example.model.enumerated.Gender;
import org.example.model.enumerated.MaritalStatus;
import org.example.service.LoanProcessingFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LoanProcessingFacade loanProcessingFacade;

    @InjectMocks
    private DealController dealController;

    private LoanStatementRequestDto validStatementRequest;
    private LoanOfferDto validLoanOffer;
    private FinishRegistrationRequestDto validFinishRequest;
    private UUID validStatementId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dealController).build();
        validStatementId = UUID.randomUUID();

        validStatementRequest = new LoanStatementRequestDto(
                BigDecimal.valueOf(100000),
                12,
                "Ivan",
                "Ivanov",
                "Ivanovich",
                "ivanov@example.com",
                LocalDate.of(1990, 1, 1),
                "1234",
                "123456"
        );

        validLoanOffer = new LoanOfferDto(
                UUID.randomUUID(),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(90000),
                12,
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(9500),
                true,
                true
        );
    }


    @Test
    void createStatement_InvalidRequest_ShouldReturn400() throws Exception {
        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void selectOffer_ValidRequest_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoanOffer)))
                .andExpect(status().isOk());

        verify(loanProcessingFacade).processOfferSelection(any(LoanOfferDto.class));
    }


    @Test
    void finishCalculateCredit_InvalidStatementId_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/deal/calculate/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFinishRequest)))
                .andExpect(status().isBadRequest());
    }

}