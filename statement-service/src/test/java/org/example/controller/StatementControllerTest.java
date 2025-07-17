package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.service.StatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StatementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StatementService statementService;

    @InjectMocks
    private StatementController statementController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(statementController)
                .setControllerAdvice(new GlobalExceptionHandler()) // если есть обработчик исключений
                .build();
    }

    @Test
    void createStatement_ShouldReturnOffers_WhenValidRequest() throws Exception {
        LoanStatementRequestDto request = createValidRequestDto();
        List<LoanOfferDto> expectedOffers = List.of(
                createValidOfferDto(UUID.randomUUID()),
                createValidOfferDto(UUID.randomUUID())
        );

        when(statementService.calculateOffers(any(LoanStatementRequestDto.class)))
                .thenReturn(expectedOffers);

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].statementId").exists())
                .andExpect(jsonPath("$[0].requestedAmount").value(100000))
                .andExpect(jsonPath("$[1].statementId").exists());

        verify(statementService, times(1)).calculateOffers(any(LoanStatementRequestDto.class));
    }

    @Test
    void createStatement_ShouldReturn400_WhenInvalidRequest() throws Exception {
        LoanStatementRequestDto invalidRequest = createValidRequestDto();
        invalidRequest.setEmail("invalid-email"); // Невалидный email

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    void selectOffer_ShouldReturn200_WhenValidRequest() throws Exception {
        LoanOfferDto offer = createValidOfferDto(UUID.randomUUID());

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isOk());

        verify(statementService, times(1)).selectOffer(any(LoanOfferDto.class));
    }

    @Test
    void selectOffer_ShouldReturn400_WhenInvalidRequest() throws Exception {
        LoanOfferDto invalidOffer = new LoanOfferDto(
                null,
                BigDecimal.valueOf(-100000),
                BigDecimal.valueOf(90000),
                -12,
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(95000),
                true,
                true
        );


        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOffer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    private LoanStatementRequestDto createValidRequestDto() {
        return new LoanStatementRequestDto(
                BigDecimal.valueOf(100000),
                12,
                "Ivan",
                "Ivanov",
                "Ivanovich",
                "ivan@example.com",
                LocalDate.of(1990, 1, 1),
                "4444",
                "123456"
        );
    }

    private LoanOfferDto createValidOfferDto(UUID applicationId) {
        return new LoanOfferDto(
                applicationId,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(90000),
                12,
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(95000),
                true,
                true
        );
    }
}