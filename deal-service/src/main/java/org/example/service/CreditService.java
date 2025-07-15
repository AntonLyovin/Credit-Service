package org.example.service;

import org.example.model.dto.ScoringDataDto;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;

import javax.naming.ServiceUnavailableException;

public interface CreditService {
    Credit createCredit(ScoringDataDto scoringData, Statement statement) throws ServiceUnavailableException;
}
