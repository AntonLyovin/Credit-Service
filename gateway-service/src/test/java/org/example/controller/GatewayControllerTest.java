package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.dto.EmploymentDto;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.service.GatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GatewayController.class)
public class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayService gatewayService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules(); // Для корректной сериализации дат
    }

    @Test
    void createStatement_ShouldReturnOffers_WhenValidRequest() throws Exception {
        LoanStatementRequestDto request = new LoanStatementRequestDto(
                BigDecimal.valueOf(100000),
                12,
                "Ivan",
                "Ivanov",
                "Ivanovich",
                "ivan@example.com",
                LocalDate.of(1990,1,1),
                null,
                null
        );

        List<LoanOfferDto> expectedOffers = List.of(
                new LoanOfferDto(UUID.randomUUID(), BigDecimal.valueOf(100000), BigDecimal.valueOf(90000), 12, BigDecimal.valueOf(8.5), BigDecimal.valueOf(95000), true, true),
                new LoanOfferDto(UUID.randomUUID(), BigDecimal.valueOf(120000), BigDecimal.valueOf(110000), 24, BigDecimal.valueOf(9.0), BigDecimal.valueOf(115000), false, true)
        );

        Mockito.when(gatewayService.calculateOffers(any(LoanStatementRequestDto.class)))
                .thenReturn(expectedOffers);

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(gatewayService).calculateOffers(any(LoanStatementRequestDto.class));
    }

    @Test
    void selectOffer_ShouldReturn200_WhenValidRequest() throws Exception {
        LoanOfferDto offer = new LoanOfferDto(
                UUID.randomUUID(),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(90000),
                12,
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(95000),
                true,
                true
        );

        mockMvc.perform(post("/statement/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isOk());

        Mockito.verify(gatewayService).selectOffer(any(LoanOfferDto.class));
    }

    @Test
    void finishCalculateCredit_ShouldReturn200() throws Exception {
        UUID statementId = UUID.randomUUID();

        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto(
                null,
                null,
                2,
                new EmploymentDto(),
                "4444",
                "666666",
                LocalDate.of(2020, 1, 1),
                "UFMS",
                null,
                false,
                false
        );

        mockMvc.perform(post("/statement/registration/{statementId}", statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(gatewayService).processCreditCalculation(eq(request), eq(statementId));
    }

    @Test
    void sendDocument_ShouldReturn200() throws Exception {
        UUID statementId = UUID.randomUUID();

        mockMvc.perform(post("/document/{statementId}", statementId))
                .andExpect(status().isOk());

        Mockito.verify(gatewayService).processSendDocuments(eq(statementId));
    }

    @Test
    void signDocument_ShouldReturn200() throws Exception {
        UUID statementId = UUID.randomUUID();

        mockMvc.perform(post("/document/{statementId}/sign", statementId))
                .andExpect(status().isOk());

        Mockito.verify(gatewayService).processSignDocuments(eq(statementId));
    }

    @Test
    void verifySesCode_ShouldReturn200() throws Exception {
        UUID statementId = UUID.randomUUID();
        String sesCode = "123456";

        mockMvc.perform(post("/document/{statementId}/code", statementId)
                        .param("sesCode", sesCode))
                .andExpect(status().isOk());

        Mockito.verify(gatewayService).processVerifySesCode(eq(statementId), eq(sesCode));
    }
}