package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.service.GatewayService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@RestController
@RequestMapping("/statement")
@Slf4j
@RequiredArgsConstructor
public class GatewayController {
    private final GatewayService gatewayService;

    @PostMapping
    @Operation(
            summary = "Микросервис заявка",
            description = "Проводит прескоринг и отправляет запрос на расчет возможных условий кредита"
    )
    public List<LoanOfferDto> createStatement(
            @RequestBody @Parameter(description = "Данные для прескоринга")
            @Valid LoanStatementRequestDto requestDto) throws ServiceUnavailableException {

        log.info("Начало расчета кредита. Тело запроса: {}", requestDto);
        return gatewayService.calculateOffers(requestDto);
    }
}
