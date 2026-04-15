package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtPayoffResponse {
    private List<String> avalancheOrder;
    private List<String> snowballOrder;
    private BigDecimal totalDebt;
    private String recommendation;
}
