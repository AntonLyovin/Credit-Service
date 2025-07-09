package org.example.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoringResultDto {
    private BigDecimal rate;
    private boolean isApproved;
    private String rejectionReason;
}