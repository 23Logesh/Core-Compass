package com.corecompass.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
class FinanceClientFallback implements FinanceClient {
    public FinanceSummaryDTO getMonthlySummary(UUID u, String m) {
        return new FinanceSummaryDTO(0, 0, 0, 0);
    }
}
