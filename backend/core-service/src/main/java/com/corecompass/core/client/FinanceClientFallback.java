package com.corecompass.core.client;

import com.corecompass.core.dto.FinanceSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
class FinanceClientFallback implements FinanceClient {
    @Override
    public FinanceSummaryDTO getMonthlySummary(UUID userId, String month) {
        log.warn("finance-service unavailable — returning empty summary for userId={}", userId);
        return FinanceSummaryDTO.builder()
                .monthlyIncome(0)
                .monthlyExpenses(0)
                .netSavings(0)
                .healthScore(0)
                .build();
    }
}
