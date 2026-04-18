package com.corecompass.finance.controller;
import com.corecompass.finance.dto.*;
import com.corecompass.finance.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController @RequiredArgsConstructor
public class FinanceController {
    private final FinanceService svc;

    // ── EXPENSE CATEGORIES (Type Registry) ──────────────────
    @GetMapping("/api/v1/finance/expense-categories")
    public ResponseEntity<ApiResponse<List<ExpenseCategoryDTO>>> listCategories(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listCategories(uid), null));
    }

    @PostMapping("/api/v1/finance/expense-categories")
    public ResponseEntity<ApiResponse<ExpenseCategoryDTO>> createCategory(
            @RequestHeader("X-User-Id") UUID uid,
            @Valid @RequestBody ExpenseCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(svc.createCategory(uid, req), "Category created"));
    }

    @PutMapping("/api/v1/finance/expense-categories/{id}")
    public ResponseEntity<ApiResponse<ExpenseCategoryDTO>> updateCategory(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateCategory(uid, id, req), "Category updated"));
    }

    @DeleteMapping("/api/v1/finance/expense-categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id) {
        svc.deleteCategory(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Category deleted"));
    }

    // ── PAYMENT METHODS ──────────────────────────────────────
    @GetMapping("/api/v1/finance/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodDTO>>> listPaymentMethods(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listPaymentMethods(uid), null));
    }
    @PostMapping("/api/v1/finance/payment-methods")
    public ResponseEntity<ApiResponse<PaymentMethodDTO>> createPaymentMethod(
            @RequestHeader("X-User-Id") UUID uid,
            @Valid @RequestBody PaymentMethodRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(svc.createPaymentMethod(uid, req), "Payment method created"));
    }

    @PutMapping("/api/v1/finance/payment-methods/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodDTO>> updatePaymentMethod(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id,
            @Valid @RequestBody PaymentMethodRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updatePaymentMethod(uid, id, req), "Updated"));
    }

    @DeleteMapping("/api/v1/finance/payment-methods/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id) {
        svc.deletePaymentMethod(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    // ── EXPENSES ─────────────────────────────────────────────
    @GetMapping("/api/v1/finance/expenses")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> listExpenses(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(required=false) UUID categoryId,
            @RequestParam(required=false) LocalDate dateFrom,
            @RequestParam(required=false) LocalDate dateTo,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listExpenses(uid, categoryId, dateFrom, dateTo,
            PageRequest.of(page, size, Sort.by("expenseDate").descending())), null));
    }

    @PostMapping("/api/v1/finance/expenses")
    public ResponseEntity<ApiResponse<ExpenseResponse>> addExpense(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody ExpenseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.addExpense(uid, req), "Expense added"));
    }

    @DeleteMapping("/api/v1/finance/expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id) {
        svc.deleteExpense(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    // ── RECURRING EXPENSES ────────────────────────────────────────────────

    @GetMapping("/api/v1/finance/expenses/recurring")
    public ResponseEntity<ApiResponse<List<RecurringExpenseResponse>>> listRecurring(
            @RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listRecurringExpenses(uid), null));
    }

    @PostMapping("/api/v1/finance/expenses/recurring")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> createRecurring(
            @RequestHeader("X-User-Id") UUID uid,
            @Valid @RequestBody RecurringExpenseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(svc.createRecurringExpense(uid, req), "Recurring expense created"));
    }

    @PutMapping("/api/v1/finance/expenses/recurring/{id}")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> updateRecurring(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id,
            @Valid @RequestBody RecurringExpenseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateRecurringExpense(uid, id, req), "Updated"));
    }

    @DeleteMapping("/api/v1/finance/expenses/recurring/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecurring(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id) {
        svc.deleteRecurringExpense(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    @PutMapping("/api/v1/finance/expenses/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateExpense(uid, id, req), "Expense updated"));
    }

    // ── INCOME ───────────────────────────────────────────────
    @GetMapping("/api/v1/finance/income")
    public ResponseEntity<ApiResponse<PageResponse<IncomeResponse>>> listIncome(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listIncome(uid, PageRequest.of(page, size)), null));
    }

    @PostMapping("/api/v1/finance/income")
    public ResponseEntity<ApiResponse<IncomeResponse>> addIncome(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody IncomeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.addIncome(uid, req), "Income added"));
    }

    @PutMapping("/api/v1/finance/income/{id}")
    public ResponseEntity<ApiResponse<IncomeResponse>> updateIncome(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id,
            @Valid @RequestBody IncomeRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateIncome(uid, id, req), "Income updated"));
    }

    @DeleteMapping("/api/v1/finance/income/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id) {
        svc.deleteIncome(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    @GetMapping("/api/v1/finance/income/sources")
    public ResponseEntity<ApiResponse<List<IncomeSourceResponse>>> getIncomeSources(
            @RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getIncomeSources(uid), null));
    }

    // ── BUDGETS ──────────────────────────────────────────────
    @GetMapping("/api/v1/finance/budgets")
    public ResponseEntity<ApiResponse<List<BudgetStatusResponse>>> getBudgets(
            @RequestHeader("X-User-Id") UUID uid, @RequestParam(required=false) String month) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getBudgetsForMonth(uid, month), null));
    }

    @PutMapping("/api/v1/finance/budgets")
    public ResponseEntity<ApiResponse<List<BudgetStatusResponse>>> setBudgets(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody List<BudgetRequest> reqs) {
        return ResponseEntity.ok(ApiResponse.ok(svc.setBudgets(uid, reqs), "Budgets updated"));
    }

    // ── HEALTH SCORE ─────────────────────────────────────────
    @GetMapping("/api/v1/finance/health-score")
    public ResponseEntity<ApiResponse<HealthScoreResponse>> getHealthScore(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getHealthScore(uid), null));
    }

    // ── NET WORTH ────────────────────────────────────────────
    @GetMapping("/api/v1/finance/net-worth")
    public ResponseEntity<ApiResponse<NetWorthResponse>> getNetWorth(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getNetWorth(uid), null));
    }

    // ── SAVINGS GOALS ────────────────────────────────────────
    @GetMapping("/api/v1/finance/savings-goals")
    public ResponseEntity<ApiResponse<List<SavingsGoalResponse>>> listSavingsGoals(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listSavingsGoals(uid), null));
    }

    @PostMapping("/api/v1/finance/savings-goals")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> createSavingsGoal(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody SavingsGoalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.createSavingsGoal(uid, req), "Savings goal created"));
    }

    @PostMapping("/api/v1/finance/savings-goals/{id}/contribute")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> contribute(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.ok(svc.contribute(uid, id, amount), "Contribution added"));
    }

    @PutMapping("/api/v1/finance/savings-goals/{id}")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> updateSavingsGoal(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id,
            @Valid @RequestBody SavingsGoalUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateSavingsGoal(uid, id, req), "Savings goal updated"));
    }

    @DeleteMapping("/api/v1/finance/savings-goals/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSavingsGoal(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id) {
        svc.deleteSavingsGoal(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Savings goal deleted"));
    }

    // ── DEBTS ────────────────────────────────────────────────
    @GetMapping("/api/v1/finance/debts")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> listDebts(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listDebts(uid), null));
    }

    @PostMapping("/api/v1/finance/debts")
    public ResponseEntity<ApiResponse<DebtResponse>> addDebt(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody DebtRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.addDebt(uid, req), "Debt added"));
    }

    @PatchMapping("/api/v1/finance/debts/{id}/payment")
    public ResponseEntity<ApiResponse<DebtResponse>> logPayment(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.ok(svc.logPayment(uid, id, amount), "Payment logged"));
    }

    @GetMapping("/api/v1/finance/debts/payoff-strategy")
    public ResponseEntity<ApiResponse<DebtPayoffResponse>> payoffStrategy(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getDebtPayoffStrategy(uid), null));
    }

    // ── INVESTMENTS ──────────────────────────────────────────
    @GetMapping("/api/v1/finance/investment-types")
    public ResponseEntity<ApiResponse<List<InvestmentTypeDTO>>> listInvestmentTypes(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listInvestmentTypes(uid), null));
    }

    @GetMapping("/api/v1/finance/investments")
    public ResponseEntity<ApiResponse<List<InvestmentResponse>>> listInvestments(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listInvestments(uid), null));
    }

    @PostMapping("/api/v1/finance/investments")
    public ResponseEntity<ApiResponse<InvestmentResponse>> addInvestment(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody InvestmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.addInvestment(uid, req), "Investment added"));
    }

    @PatchMapping("/api/v1/finance/investments/{id}/value")
    public ResponseEntity<ApiResponse<InvestmentResponse>> updateInvestmentValue(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id, @RequestParam BigDecimal value) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateValue(uid, id, value), "Value updated"));
    }

    @GetMapping("/api/v1/finance/investments/summary")
    public ResponseEntity<ApiResponse<InvestmentSummaryResponse>> investmentSummary(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getInvestmentSummary(uid), null));
    }

    // ── ANALYTICS ────────────────────────────────────────────
    @GetMapping("/api/v1/finance/analytics/spending-patterns")
    public ResponseEntity<ApiResponse<SpendingPatternResponse>> spendingPatterns(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getSpendingPatterns(uid), null));
    }

    // ── INTERNAL FEIGN ENDPOINT ──────────────────────────────
    @GetMapping("/internal/finance/summary/monthly")
    public ResponseEntity<FinanceSummaryDTO> internalMonthlySummary(@RequestParam UUID userId, @RequestParam String month) {
        return ResponseEntity.ok(svc.getMonthlySummary(userId, month));
    }
}
