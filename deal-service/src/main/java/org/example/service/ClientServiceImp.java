package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.entity.Client;
import org.example.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ClientServiceImp implements ClientService{
    private final ClientRepository clientRepository;

    public ClientServiceImp(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public Client createClient(LoanStatementRequestDto requestDto) {
        Client client = Client.builder()
                .lastName(requestDto.getLastName())
                .firstName(requestDto.getFirstName())
                .middleName(requestDto.getMiddleName())
                .birthDate(requestDto.getBirthdate())
                .email(requestDto.getEmail())
                .build();

        return clientRepository.save(client);
    }
}
