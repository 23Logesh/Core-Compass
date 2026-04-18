package com.corecompass.finance.service;
import com.corecompass.finance.dto.*;
import com.corecompass.finance.entity.*;
import com.corecompass.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import com.corecompass.finance.dto.ExpenseCategoryRequest;
import com.corecompass.finance.dto.IncomeSourceResponse;
import com.corecompass.finance.dto.SavingsGoalUpdateRequest;

@Slf4j @Service @RequiredArgsConstructor
public class FinanceService {
    private final ExpenseRepository         expenseRepo;
    private final IncomeRepository          incomeRepo;
    private final BudgetRepository          budgetRepo;
    private final SavingsGoalRepository     savingsRepo;
    private final DebtRepository            debtRepo;
    private final InvestmentRepository      investmentRepo;
    private final InvestmentTypeRepository  investmentTypeRepo;
    private final ExpenseCategoryRepository    categoryRepo;
    private final PaymentMethodRepository      paymentMethodRepo;
    private final RecurringExpenseRepository   recurringExpenseRepo;
    private final com.corecompass.finance.client.NotificationClient notificationClient;

    // ─── EXPENSE CATEGORIES ───────────────────────────────────
    public List<ExpenseCategoryDTO> listCategories(UUID userId) {
        return categoryRepo.findAvailableForUser(userId).stream().map(c ->
            ExpenseCategoryDTO.builder().id(c.getId()).name(c.getName())
                .icon(c.getIcon()).color(c.getColor()).isSystem(c.isSystem()).build()
        ).collect(Collectors.toList());
    }

    // ─── PAYMENT METHODS ──────────────────────────────────────
    public List<PaymentMethodDTO> listPaymentMethods(UUID userId) {
        return paymentMethodRepo.findAvailableForUser(userId).stream().map(p ->
            PaymentMethodDTO.builder().id(p.getId()).name(p.getName())
                .icon(p.getIcon()).isSystem(p.isSystem()).build()
        ).collect(Collectors.toList());
    }

    // ─── EXPENSES ─────────────────────────────────────────────
    public PageResponse<ExpenseResponse> listExpenses(UUID userId, UUID categoryId, LocalDate from, LocalDate to, Pageable p) {
        return PageResponse.of(expenseRepo.findFiltered(userId, categoryId, from, to, p).map(this::toExpenseResp));
    }

    @Transactional
    public ExpenseResponse addExpense(UUID userId, ExpenseRequest req) {
        ExpenseEntity e = ExpenseEntity.builder()
                .userId(userId).amount(req.getAmount()).categoryId(req.getCategoryId())
                .subCategoryId(req.getSubCategoryId()).paymentMethodId(req.getPaymentMethodId())
                .expenseDate(req.getDate() != null ? req.getDate() : LocalDate.now())
                .merchant(req.getMerchant()).note(req.getNote())
                .tags(req.getTags()).isRecurring(req.isRecurring()).build();

        ExpenseResponse saved = toExpenseResp(expenseRepo.save(e));
        log.info("Expense added: {} userId={}", e.getAmount(), userId);

        // Check budget alert after saving — fire only on threshold crossing
        checkBudgetAlert(userId, req.getCategoryId(), req.getAmount());

        return saved;
    }

    @Transactional
    public void deleteExpense(UUID userId, UUID id) {
        ExpenseEntity e = expenseRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found or not owned by you"));
        e.setDeleted(true);
        expenseRepo.save(e);
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID userId, UUID id, ExpenseRequest req) {
        ExpenseEntity e = expenseRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found or not owned by you"));
        if (e.isDeleted()) throw new RuntimeException("Expense not found");
        e.setAmount(req.getAmount());
        e.setCategoryId(req.getCategoryId());
        if (req.getSubCategoryId() != null) e.setSubCategoryId(req.getSubCategoryId());
        if (req.getPaymentMethodId() != null) e.setPaymentMethodId(req.getPaymentMethodId());
        if (req.getDate() != null) e.setExpenseDate(req.getDate());
        if (req.getMerchant() != null) e.setMerchant(req.getMerchant());
        if (req.getNote() != null) e.setNote(req.getNote());
        if (req.getTags() != null) e.setTags(req.getTags());
        e.setRecurring(req.isRecurring());
        return toExpenseResp(expenseRepo.save(e));
    }

    @Transactional
    public IncomeResponse updateIncome(UUID userId, UUID id, IncomeRequest req) {
        IncomeEntity e = incomeRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Income not found or not owned by you"));
        if (e.isDeleted()) throw new RuntimeException("Income not found");
        e.setAmount(req.getAmount());
        e.setSourceType(req.getSourceType());
        if (req.getNote() != null) e.setNote(req.getNote());
        if (req.getDate() != null) e.setIncomeDate(req.getDate());
        e.setRecurring(req.isRecurring());
        return toIncomeResp(incomeRepo.save(e));
    }

    @Transactional
    public void deleteIncome(UUID userId, UUID id) {
        IncomeEntity e = incomeRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Income not found or not owned by you"));
        e.setDeleted(true);
        incomeRepo.save(e);
    }

    // ─── RECURRING EXPENSES ───────────────────────────────────

    public List<RecurringExpenseResponse> listRecurringExpenses(UUID userId) {
        return recurringExpenseRepo
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toRecurringResp)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecurringExpenseResponse createRecurringExpense(UUID userId, RecurringExpenseRequest req) {
        RecurringExpenseEntity e = RecurringExpenseEntity.builder()
                .userId(userId)
                .amount(req.getAmount())
                .categoryId(req.getCategoryId())
                .subCategoryId(req.getSubCategoryId())
                .paymentMethodId(req.getPaymentMethodId())
                .merchant(req.getMerchant())
                .note(req.getNote())
                .frequency(req.getFrequency())
                .dayOfPeriod(req.getDayOfPeriod())
                .startsOn(req.getStartsOn())
                .endsOn(req.getEndsOn())
                .build();
        log.info("Recurring expense created: {} {} userId={}", e.getAmount(), e.getFrequency(), userId);
        return toRecurringResp(recurringExpenseRepo.save(e));
    }

    @Transactional
    public RecurringExpenseResponse updateRecurringExpense(UUID userId, UUID id, RecurringExpenseRequest req) {
        RecurringExpenseEntity e = recurringExpenseRepo.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring expense not found or not owned by you"));
        e.setAmount(req.getAmount());
        e.setCategoryId(req.getCategoryId());
        if (req.getSubCategoryId()    != null) e.setSubCategoryId(req.getSubCategoryId());
        if (req.getPaymentMethodId()  != null) e.setPaymentMethodId(req.getPaymentMethodId());
        if (req.getMerchant()         != null) e.setMerchant(req.getMerchant());
        if (req.getNote()             != null) e.setNote(req.getNote());
        e.setFrequency(req.getFrequency());
        if (req.getDayOfPeriod()      != null) e.setDayOfPeriod(req.getDayOfPeriod());
        if (req.getStartsOn()         != null) e.setStartsOn(req.getStartsOn());
        if (req.getEndsOn()           != null) e.setEndsOn(req.getEndsOn());
        return toRecurringResp(recurringExpenseRepo.save(e));
    }

    @Transactional
    public void deleteRecurringExpense(UUID userId, UUID id) {
        RecurringExpenseEntity e = recurringExpenseRepo.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring expense not found or not owned by you"));
        e.setDeleted(true);
        recurringExpenseRepo.save(e);
    }

    // ─── INCOME ───────────────────────────────────────────────
    public PageResponse<IncomeResponse> listIncome(UUID userId, Pageable p) {
        return PageResponse.of(incomeRepo.findByUserIdAndIsDeletedFalseOrderByIncomeDateDesc(userId, p).map(this::toIncomeResp));
    }

    @Transactional
    public IncomeResponse addIncome(UUID userId, IncomeRequest req) {
        IncomeEntity e = IncomeEntity.builder().userId(userId).amount(req.getAmount())
            .sourceType(req.getSourceType()).note(req.getNote())
            .incomeDate(req.getDate() != null ? req.getDate() : LocalDate.now())
            .isRecurring(req.isRecurring()).build();
        return toIncomeResp(incomeRepo.save(e));
    }

    // ─── BUDGETS ──────────────────────────────────────────────
    public List<BudgetStatusResponse> getBudgetsForMonth(UUID userId, String monthYear) {
        String month = monthYear != null ? monthYear : YearMonth.now().toString();
        LocalDate from = LocalDate.parse(month + "-01");
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        List<BudgetEntity> budgets = budgetRepo.findByUserIdAndBudgetMonth(userId, month);

        // Load all needed categories in ONE query
        Set<UUID> catIds = budgets.stream().map(BudgetEntity::getCategoryId).collect(Collectors.toSet());
        Map<UUID, ExpenseCategoryEntity> catMap = categoryRepo.findAllById(catIds)
                .stream().collect(Collectors.toMap(ExpenseCategoryEntity::getId, c -> c));

        return budgets.stream().map(b -> {
            BigDecimal spent  = orZero(expenseRepo.sumByCategoryAndDateRange(userId, b.getCategoryId(), from, to));
            BigDecimal remain = b.getAmountLimit().subtract(spent);
            double pct = b.getAmountLimit().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(b.getAmountLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
            ExpenseCategoryEntity cat = catMap.get(b.getCategoryId());
            return BudgetStatusResponse.builder()
                    .categoryId(b.getCategoryId())
                    .categoryName(cat != null ? cat.getName() : "Unknown")
                    .categoryIcon(cat != null ? cat.getIcon() : null)
                    .monthYear(month)
                    .budgetAmount(b.getAmountLimit())
                    .spentAmount(spent)
                    .remainingAmount(remain)
                    .percentageUsed(pct)
                    .exceeded(remain.compareTo(BigDecimal.ZERO) < 0)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<BudgetStatusResponse> setBudgets(UUID userId, List<BudgetRequest> reqs) {
        String month = YearMonth.now().toString();
        reqs.forEach(req -> budgetRepo.findByUserIdAndCategoryIdAndBudgetMonth(userId, req.getCategoryId(), month)
            .ifPresentOrElse(b -> { b.setAmountLimit(req.getBudgetAmount()); budgetRepo.save(b); },
                () -> budgetRepo.save(BudgetEntity.builder().userId(userId)
                    .categoryId(req.getCategoryId()).budgetMonth(month).amountLimit(req.getBudgetAmount()).build())));
        return getBudgetsForMonth(userId, month);
    }

    // ─── HEALTH SCORE (0-100, 4 components per API doc) ───────
    public HealthScoreResponse getHealthScore(UUID userId) {
        String month = YearMonth.now().toString();
        LocalDate from = LocalDate.parse(month + "-01");
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        BigDecimal income   = orZero(incomeRepo.sumByUserAndDateRange(userId, from, to));
        BigDecimal expenses = orZero(expenseRepo.sumByUserAndDateRange(userId, from, to));
        BigDecimal debt     = orZero(debtRepo.sumCurrentBalanceByUser(userId));
        BigDecimal net      = income.subtract(expenses);

        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
            ? net.divide(income, 4, RoundingMode.HALF_UP).doubleValue() : 0;
        double dti = income.compareTo(BigDecimal.ZERO) > 0
            ? debt.divide(income.multiply(BigDecimal.valueOf(12)), 4, RoundingMode.HALF_UP).doubleValue() : 1.0;
        List<BudgetStatusResponse> budgets = getBudgetsForMonth(userId, month);
        long exceeded = budgets.stream().filter(BudgetStatusResponse::isExceeded).count();
        double budgetAdherence = budgets.isEmpty() ? 1.0
            : (double)(budgets.size() - exceeded) / budgets.size();

        // 30pts savings, 25pts budget, 25pts debt, 20pts expense<income
        int score = 0;
        score += (int) Math.min(30, savingsRate / 0.30 * 30);
        score += (int) Math.min(25, budgetAdherence * 25);
        score += dti <= 0.2 ? 25 : (int) Math.max(0, (0.5 - dti) / 0.3 * 25);
        if (expenses.compareTo(income) <= 0) score += 20;

        String grade = score >= 90 ? "A+" : score >= 80 ? "A" : score >= 70 ? "B+" :
                       score >= 60 ? "B"  : score >= 50 ? "C" : "D";

        return HealthScoreResponse.builder()
            .score(Math.min(100, Math.max(0, score))).grade(grade)
            .monthlyIncome(income).monthlyExpenses(expenses).netSavings(net).totalDebt(debt)
            .savingsRatePct(savingsRate * 100).debtToIncomePct(dti * 100).build();
    }

    // ─── NET WORTH ────────────────────────────────────────────
    public NetWorthResponse getNetWorth(UUID userId) {
        String month = YearMonth.now().toString();
        // Assets = savings goals current + investments current value
        BigDecimal savingsAssets = savingsRepo.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
            .stream().map(SavingsGoalEntity::getCurrentAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Object[] invSummary = investmentRepo.getSummaryByUser(userId);
        BigDecimal invCurrent = invSummary[1] != null ? new BigDecimal(invSummary[1].toString()) : BigDecimal.ZERO;
        BigDecimal totalAssets = savingsAssets.add(invCurrent);
        BigDecimal totalDebt   = orZero(debtRepo.sumCurrentBalanceByUser(userId));
        return NetWorthResponse.builder().totalAssets(totalAssets).totalLiabilities(totalDebt)
            .netWorth(totalAssets.subtract(totalDebt)).month(month).build();
    }

    // ─── SAVINGS GOALS ────────────────────────────────────────
    public List<SavingsGoalResponse> listSavingsGoals(UUID userId) {
        return savingsRepo.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
            .stream().map(this::toSavingsResp).collect(Collectors.toList());
    }
    @Transactional
    public SavingsGoalResponse createSavingsGoal(UUID userId, SavingsGoalRequest req) {
        SavingsGoalEntity e = SavingsGoalEntity.builder().userId(userId).title(req.getTitle())
            .targetAmount(req.getTargetAmount()).targetDate(req.getTargetDate()).build();
        return toSavingsResp(savingsRepo.save(e));
    }
    @Transactional
    public SavingsGoalResponse contribute(UUID userId, UUID goalId, BigDecimal amount) {
        SavingsGoalEntity e = savingsRepo.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new RuntimeException("Savings goal not found"));
        e.setCurrentAmount(e.getCurrentAmount().add(amount));
        return toSavingsResp(savingsRepo.save(e));
    }

    // ─── DEBTS ────────────────────────────────────────────────
    public List<DebtResponse> listDebts(UUID userId) {
        return debtRepo.findByUserIdAndIsDeletedFalseOrderByCurrentBalanceDesc(userId)
            .stream().map(this::toDebtResp).collect(Collectors.toList());
    }
    @Transactional
    public DebtResponse addDebt(UUID userId, DebtRequest req) {
        DebtEntity e = DebtEntity.builder().userId(userId).name(req.getName())
            .debtType(req.getDebtType()).principalAmount(req.getPrincipalAmount())
            .currentBalance(req.getCurrentBalance() != null ? req.getCurrentBalance() : req.getPrincipalAmount())
            .interestRate(req.getInterestRate()).minPayment(req.getMinPayment()).build();
        return toDebtResp(debtRepo.save(e));
    }
    @Transactional
    public DebtResponse logPayment(UUID userId, UUID debtId, BigDecimal paymentAmount) {
        DebtEntity e = debtRepo.findByIdAndUserId(debtId, userId)
            .orElseThrow(() -> new RuntimeException("Debt not found"));
        BigDecimal newBalance = e.getCurrentBalance().subtract(paymentAmount);
        e.setCurrentBalance(newBalance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newBalance);
        if (e.getCurrentBalance().compareTo(BigDecimal.ZERO) == 0) e.setDeleted(true);
        return toDebtResp(debtRepo.save(e));
    }
    public DebtPayoffResponse getDebtPayoffStrategy(UUID userId) {
        List<DebtEntity> debts = debtRepo.findByUserIdAndIsDeletedFalseOrderByCurrentBalanceDesc(userId);
        List<String> avalanche = debts.stream()
            .sorted((a,b) -> b.getInterestRate() != null && a.getInterestRate() != null
                ? b.getInterestRate().compareTo(a.getInterestRate()) : 0)
            .map(DebtEntity::getName).collect(Collectors.toList());
        List<String> snowball  = debts.stream()
            .sorted((a,b) -> a.getCurrentBalance().compareTo(b.getCurrentBalance()))
            .map(DebtEntity::getName).collect(Collectors.toList());
        BigDecimal total = debts.stream().map(DebtEntity::getCurrentBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        return DebtPayoffResponse.builder().avalancheOrder(avalanche).snowballOrder(snowball)
            .totalDebt(total).recommendation(avalanche.isEmpty() ? "Debt-free! Great job!" :
                "Avalanche saves most interest. Pay \"" + avalanche.get(0) + "\" first.").build();
    }

    // ─── INVESTMENTS ──────────────────────────────────────────
    public List<InvestmentTypeDTO> listInvestmentTypes(UUID userId) {
        return investmentTypeRepo.findAvailableForUser(userId).stream().map(t ->
            InvestmentTypeDTO.builder().id(t.getId()).name(t.getName()).icon(t.getIcon()).isSystem(t.isSystem()).build()
        ).collect(Collectors.toList());
    }
    public List<InvestmentResponse> listInvestments(UUID userId) {
        return investmentRepo.findByUserIdAndIsDeletedFalseOrderByPurchaseDateDesc(userId)
            .stream().map(this::toInvestmentResp).collect(Collectors.toList());
    }
    @Transactional
    public InvestmentResponse addInvestment(UUID userId, InvestmentRequest req) {
        InvestmentEntity e = InvestmentEntity.builder().userId(userId)
            .investmentTypeId(req.getInvestmentTypeId()).name(req.getName())
            .investedAmount(req.getInvestedAmount()).currentValue(req.getCurrentValue())
            .purchaseDate(req.getPurchaseDate()).maturityDate(req.getMaturityDate())
            .notes(req.getNotes()).build();
        return toInvestmentResp(investmentRepo.save(e));
    }
    @Transactional
    public InvestmentResponse updateValue(UUID userId, UUID id, BigDecimal newValue) {
        InvestmentEntity e = investmentRepo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Investment not found"));
        e.setCurrentValue(newValue);
        return toInvestmentResp(investmentRepo.save(e));
    }
    public InvestmentSummaryResponse getInvestmentSummary(UUID userId) {
        Object[] row = investmentRepo.getSummaryByUser(userId);
        BigDecimal invested = row[0] != null ? new BigDecimal(row[0].toString()) : BigDecimal.ZERO;
        BigDecimal current  = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
        double ret = invested.compareTo(BigDecimal.ZERO) > 0
            ? current.subtract(invested).divide(invested, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
        int count = investmentRepo.findByUserIdAndIsDeletedFalseOrderByPurchaseDateDesc(userId).size();
        return InvestmentSummaryResponse.builder().totalInvested(invested)
            .totalCurrentValue(current).returnsPercent(ret).count(count).build();
    }

    // ─── SPENDING ANALYTICS ───────────────────────────────────
    public SpendingPatternResponse getSpendingPatterns(UUID userId) {
        LocalDate from = LocalDate.now().minusMonths(3);
        List<Object[]> dayStats = expenseRepo.getDayOfWeekSpending(userId, from);
        BigDecimal weekdayTotal = BigDecimal.ZERO, weekendTotal = BigDecimal.ZERO;
        int weekdayCount = 0, weekendCount = 0;
        String peakDay = "Monday";
        BigDecimal peakAmount = BigDecimal.ZERO;
        String[] days = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        for (Object[] row : dayStats) {
            int dow = ((Number) row[0]).intValue();
            BigDecimal total = new BigDecimal(row[1].toString());
            boolean isWeekend = (dow == 0 || dow == 6);
            if (isWeekend) { weekendTotal = weekendTotal.add(total); weekendCount++; }
            else           { weekdayTotal = weekdayTotal.add(total); weekdayCount++; }
            if (total.compareTo(peakAmount) > 0) { peakAmount = total; peakDay = days[dow]; }
        }
        BigDecimal weekdayAvg = weekdayCount > 0 ? weekdayTotal.divide(BigDecimal.valueOf(weekdayCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal weekendAvg = weekendCount > 0 ? weekendTotal.divide(BigDecimal.valueOf(weekendCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        List<Object[]> merchants = expenseRepo.getTopMerchants(userId, from, PageRequest.of(0, 5));
        List<SpendingPatternResponse.MerchantSpend> topMerchants = merchants.stream()
            .map(r -> new SpendingPatternResponse.MerchantSpend(r[0].toString(), new BigDecimal(r[1].toString())))
            .collect(Collectors.toList());

        List<Object[]> trend = expenseRepo.getMonthlyTrend(userId, LocalDate.now().minusMonths(6));
        List<SpendingPatternResponse.MonthlyTrend> monthlyTrend = trend.stream()
            .map(r -> new SpendingPatternResponse.MonthlyTrend(r[0].toString(), new BigDecimal(r[1].toString())))
            .collect(Collectors.toList());

        return SpendingPatternResponse.builder().weekdayAvg(weekdayAvg).weekendAvg(weekendAvg)
            .peakSpendingDay(peakDay).topMerchants(topMerchants).monthlyTrend(monthlyTrend).build();
    }

    // ─── FEIGN ENDPOINT (Report + Core) ─────────────────────
    public FinanceSummaryDTO getMonthlySummary(UUID userId, String month) {
        LocalDate from = LocalDate.parse(month + "-01");
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        BigDecimal income   = orZero(incomeRepo.sumByUserAndDateRange(userId, from, to));
        BigDecimal expenses = orZero(expenseRepo.sumByUserAndDateRange(userId, from, to));
        return FinanceSummaryDTO.builder().monthlyIncome(income.doubleValue())
            .monthlyExpenses(expenses.doubleValue()).netSavings(income.subtract(expenses).doubleValue())
            .healthScore(getHealthScore(userId).getScore()).build();
    }

    // ── EXPENSE CATEGORY CRUD ────────────────────────────────────

    @Transactional
    public ExpenseCategoryDTO createCategory(UUID userId, ExpenseCategoryRequest req) {
        if (categoryRepo.existsByNameAndCreatedBy(req.getName(), userId)) {
            throw new IllegalArgumentException("You already have a category named '" + req.getName() + "'");
        }
        ExpenseCategoryEntity e = ExpenseCategoryEntity.builder()
                .name(req.getName().trim())
                .icon(req.getIcon())
                .color(req.getColor())
                .parentId(req.getParentId())
                .isSystem(false)
                .createdBy(userId)
                .build();
        return toCategoryDTO(categoryRepo.save(e));
    }

    @Transactional
    public ExpenseCategoryDTO updateCategory(UUID userId, UUID categoryId, ExpenseCategoryRequest req) {
        ExpenseCategoryEntity e = categoryRepo.findByIdAndCreatedBy(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or not editable"));
        if (req.getName()  != null) e.setName(req.getName().trim());
        if (req.getIcon()  != null) e.setIcon(req.getIcon());
        if (req.getColor() != null) e.setColor(req.getColor());
        return toCategoryDTO(categoryRepo.save(e));
    }

    @Transactional
    public void deleteCategory(UUID userId, UUID categoryId) {
        ExpenseCategoryEntity e = categoryRepo.findByIdAndCreatedBy(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or not editable"));
        categoryRepo.delete(e);
    }

// ── INCOME SOURCES ────────────────────────────────────────────

    public List<IncomeSourceResponse> getIncomeSources(UUID userId) {
        return incomeRepo.findSourceSummaries(userId).stream()
                .map(row -> IncomeSourceResponse.builder()
                        .sourceType((String)  row[0])
                        .timesLogged((Long)   row[1])
                        .totalAmount((java.math.BigDecimal) row[2])
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

// ── SAVINGS GOAL UPDATE + DELETE ──────────────────────────────

    @Transactional
    public SavingsGoalResponse updateSavingsGoal(UUID userId, UUID goalId, SavingsGoalUpdateRequest req) {
        SavingsGoalEntity e = savingsRepo.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Savings goal not found"));
        if (req.getTitle()        != null) e.setTitle(req.getTitle());
        if (req.getTargetAmount() != null) e.setTargetAmount(req.getTargetAmount());
        if (req.getTargetDate()   != null) e.setTargetDate(req.getTargetDate());
        return toSavingsResp(savingsRepo.save(e));
    }

    @Transactional
    public void deleteSavingsGoal(UUID userId, UUID goalId) {
        SavingsGoalEntity e = savingsRepo.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Savings goal not found"));
        e.setDeleted(true);
        savingsRepo.save(e);
    }

    // ─── MAPPERS ──────────────────────────────────────────────
    private ExpenseResponse toExpenseResp(ExpenseEntity e) {
        ExpenseCategoryEntity cat = categoryRepo.findById(e.getCategoryId()).orElse(null);
        String catName = cat != null ? cat.getName() : "Unknown";
        String catIcon = cat != null ? cat.getIcon() : null;
        String pmName  = e.getPaymentMethodId() != null ? paymentMethodRepo.findById(e.getPaymentMethodId())
                .map(PaymentMethodEntity::getName).orElse(null) : null;
        return ExpenseResponse.builder().id(e.getId()).amount(e.getAmount())
            .categoryId(e.getCategoryId()).categoryName(catName).categoryIcon(catIcon)
            .paymentMethodId(e.getPaymentMethodId()).paymentMethodName(pmName)
            .expenseDate(e.getExpenseDate()).merchant(e.getMerchant()).note(e.getNote())
            .tags(e.getTags()).isRecurring(e.isRecurring()).createdAt(e.getCreatedAt()).build();
    }
    private IncomeResponse toIncomeResp(IncomeEntity e) {
        return IncomeResponse.builder().id(e.getId()).amount(e.getAmount()).sourceType(e.getSourceType())
            .note(e.getNote()).incomeDate(e.getIncomeDate()).isRecurring(e.isRecurring()).createdAt(e.getCreatedAt()).build();
    }
    private SavingsGoalResponse toSavingsResp(SavingsGoalEntity e) {
        double pct = e.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
            ? e.getCurrentAmount().divide(e.getTargetAmount(),4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
        return SavingsGoalResponse.builder().id(e.getId()).title(e.getTitle())
            .targetAmount(e.getTargetAmount()).currentAmount(e.getCurrentAmount())
            .targetDate(e.getTargetDate()).progressPct(pct).createdAt(e.getCreatedAt()).build();
    }

    private ExpenseCategoryDTO toCategoryDTO(ExpenseCategoryEntity e) {
        return ExpenseCategoryDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .icon(e.getIcon())
                .color(e.getColor())
                .isSystem(e.isSystem())
                .build();
    }

    private DebtResponse toDebtResp(DebtEntity e) {
        return DebtResponse.builder().id(e.getId()).name(e.getName()).debtType(e.getDebtType())
            .principalAmount(e.getPrincipalAmount()).currentBalance(e.getCurrentBalance())
            .interestRate(e.getInterestRate()).minPayment(e.getMinPayment()).createdAt(e.getCreatedAt()).build();
    }
    private InvestmentResponse toInvestmentResp(InvestmentEntity e) {
        String typeName = investmentTypeRepo.findById(e.getInvestmentTypeId()).map(InvestmentTypeEntity::getName).orElse("Unknown");
        Double ret = null;
        if (e.getCurrentValue() != null && e.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0) {
            ret = e.getCurrentValue().subtract(e.getInvestedAmount())
                .divide(e.getInvestedAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
        }
        return InvestmentResponse.builder().id(e.getId()).investmentTypeName(typeName).name(e.getName())
            .investedAmount(e.getInvestedAmount()).currentValue(e.getCurrentValue())
            .returnsPercent(ret).purchaseDate(e.getPurchaseDate()).createdAt(e.getCreatedAt()).build();
    }
    /**
     * Fires a budget alert only when the new expense causes the spend to
     * CROSS a threshold (80% or 100%). Comparing before vs after prevents
     * repeated notifications on every single expense.
     */
    private void checkBudgetAlert(UUID userId, UUID categoryId, BigDecimal addedAmount) {
        if (categoryId == null || addedAmount == null) return;

        String month = YearMonth.now().toString();
        budgetRepo.findByUserIdAndCategoryIdAndBudgetMonth(userId, categoryId, month)
                .ifPresent(budget -> {
                    LocalDate from = LocalDate.parse(month + "-01");
                    LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

                    // Total spend NOW (includes the expense we just saved)
                    BigDecimal spentNow = orZero(
                            expenseRepo.sumByCategoryAndDateRange(userId, categoryId, from, to));

                    // Spend BEFORE this expense
                    BigDecimal spentBefore = spentNow.subtract(addedAmount);

                    BigDecimal limit = budget.getAmountLimit();
                    if (limit.compareTo(BigDecimal.ZERO) <= 0) return;

                    double pctBefore = spentBefore.divide(limit, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    double pctNow    = spentNow.divide(limit, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();

                    String catName = categoryRepo.findById(categoryId)
                            .map(ExpenseCategoryEntity::getName).orElse("this category");

                    // Crossed 100% — budget exceeded
                    if (pctBefore < 100.0 && pctNow >= 100.0) {
                        notificationClient.createNotification(
                                userId,
                                "BUDGET_ALERT",
                                "🚨 Budget Exceeded: " + catName,
                                String.format("You've spent ₹%.0f of your ₹%.0f %s budget (%.0f%%).",
                                        spentNow, limit, catName, pctNow)
                        );
                        log.info("Budget exceeded alert sent: userId={} category={}", userId, catName);

                        // Crossed 80% warning — only if not yet exceeded
                    } else if (pctBefore < 80.0 && pctNow >= 80.0) {
                        notificationClient.createNotification(
                                userId,
                                "BUDGET_ALERT",
                                "⚠️ Budget Warning: " + catName,
                                String.format("You've used %.0f%% of your ₹%.0f %s budget. ₹%.0f remaining.",
                                        pctNow, limit, catName,
                                        limit.subtract(spentNow).max(BigDecimal.ZERO))
                        );
                        log.info("Budget 80%% warning sent: userId={} category={}", userId, catName);
                    }
                });
    }

    private BigDecimal orZero(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    private RecurringExpenseResponse toRecurringResp(RecurringExpenseEntity e) {
        ExpenseCategoryEntity cat = categoryRepo.findById(e.getCategoryId()).orElse(null);
        String catName = cat != null ? cat.getName() : "Unknown";
        String catIcon = cat != null ? cat.getIcon() : null;
        String pmName  = e.getPaymentMethodId() != null
                ? paymentMethodRepo.findById(e.getPaymentMethodId())
                .map(PaymentMethodEntity::getName).orElse(null)
                : null;
        return RecurringExpenseResponse.builder()
                .id(e.getId())
                .amount(e.getAmount())
                .categoryId(e.getCategoryId()).categoryName(catName).categoryIcon(catIcon)
                .paymentMethodId(e.getPaymentMethodId()).paymentMethodName(pmName)
                .merchant(e.getMerchant()).note(e.getNote())
                .frequency(e.getFrequency()).dayOfPeriod(e.getDayOfPeriod())
                .startsOn(e.getStartsOn()).endsOn(e.getEndsOn())
                .isActive(e.isActive()).createdAt(e.getCreatedAt())
                .build();
    }
}
