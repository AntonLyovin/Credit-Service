package org.example.service;

import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.entity.Client;
import org.example.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImpTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImp clientService;

    @Test
    @Transactional
    void createClient_Success() {
        LoanStatementRequestDto requestDto = createTestRequestDto();
        Client expectedClient = createTestClient();

        when(clientRepository.save(any(Client.class))).thenReturn(expectedClient);

        Client result = clientService.createClient(requestDto);

        assertNotNull(result);
        assertNotNull(result.getClientId());
        assertEquals(requestDto.getLastName(), result.getLastName());
        assertEquals(requestDto.getFirstName(), result.getFirstName());
        assertEquals(requestDto.getMiddleName(), result.getMiddleName());
        assertEquals(requestDto.getBirthdate(), result.getBirthDate());
        assertEquals(requestDto.getEmail(), result.getEmail());

        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @Transactional
    void createClient_WithNullMiddleName() {
        LoanStatementRequestDto requestDto = createTestRequestDto();
        requestDto.setMiddleName(null);
        Client expectedClient = createTestClient();
        expectedClient.setMiddleName(null);

        when(clientRepository.save(any(Client.class))).thenReturn(expectedClient);

        Client result = clientService.createClient(requestDto);

        assertNotNull(result);
        assertNull(result.getMiddleName());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @Transactional
    void createClient_VerifyRepositoryCall() {
        LoanStatementRequestDto requestDto = createTestRequestDto();
        Client expectedClient = createTestClient();

        when(clientRepository.save(any(Client.class))).thenReturn(expectedClient);

        clientService.createClient(requestDto);

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    private LoanStatementRequestDto createTestRequestDto() {
        return LoanStatementRequestDto.builder()
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthdate(LocalDate.of(1990, 5, 15))
                .email("ivanov@example.com")
                .build();
    }

    private Client createTestClient() {
        return Client.builder()
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 5, 15))
                .email("ivanov@example.com")
                .build();
    }
}