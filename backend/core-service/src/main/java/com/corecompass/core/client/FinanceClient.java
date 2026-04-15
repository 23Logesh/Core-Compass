package com.corecompass.core.client;

import com.corecompass.core.dto.FinanceSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// FINANCE SERVICE FEIGN CLIENT
// ─────────────────────────────────────────────────────────────
@FeignClient(
        name = "finance-service",
        path = "/internal/finance",
        fallback = FinanceClientFallback.class
)
public interface FinanceClient {

    @GetMapping("/summary/monthly")
    FinanceSummaryDTO getMonthlySummary(
            @RequestParam UUID userId,
            @RequestParam String month  // YYYY-MM
    );
}
