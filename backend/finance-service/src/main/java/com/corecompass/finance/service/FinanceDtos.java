//package com.corecompass.finance.service;
//
//import com.corecompass.finance.entity.*;
//import com.corecompass.finance.repository.*;
//import lombok.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.repository.*;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.*;
//import org.springframework.transaction.annotation.Transactional;
//
//import jakarta.validation.constraints.*;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
//// ═══════════════ DTOs ═══════════════════════════════════════════
//
//@Data class ExpenseRequest {
//    @NotNull @DecimalMin("0.01") BigDecimal amount;
//    @NotNull UUID categoryId;
//    UUID subCategoryId, paymentMethodId;
//    @NotNull LocalDate date;
//    @Size(max=100) String merchant;
//    @Size(max=200) String note;
//    boolean isRecurring;
//}
//
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//class ExpenseResponse {
//    UUID id; BigDecimal amount; String categoryName, categoryIcon;
//    String subCategoryName, paymentMethodName;
//    LocalDate date; String merchant, note; boolean isRecurring; Instant createdAt;
//}
//
//@Data class IncomeRequest {
//    @NotNull @DecimalMin("0.01") BigDecimal amount;
//    @NotBlank @Size(max=60) String sourceType;
//    String description; @NotNull LocalDate incomeDate; boolean isRecurring;
//}
//
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//class IncomeResponse {
//    UUID id; BigDecimal amount; String sourceType, description; LocalDate incomeDate; Instant createdAt;
//}
//
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//class BudgetResponse {
//    UUID id, categoryId; String categoryName, categoryIcon;
//    BigDecimal budgetAmount, spentAmount, remainingAmount; double usedPct; String month;
//}
//
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//class HealthScoreResponse {
//    int score;
//    int savingsScore; int debtScore; int budgetScore; int netWorthScore;
//    String grade;
//}
//
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//class MonthlySummaryDTO {
//    double monthlyIncome; double monthlyExpenses; double netSavings; int healthScore;
//}
//
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//class ApiResponse<T> {
//    boolean success; T data; String message;
//    @Builder.Default Instant timestamp = Instant.now();
//    static <T> ApiResponse<T> ok(T d, String m){return ApiResponse.<T>builder().success(true).data(d).message(m).build();}
//    static <T> ApiResponse<T> err(String m){return ApiResponse.<T>builder().success(false).message(m).build();}
//}
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//class PageResponse<T> {
//    List<T> content; int page,size; long totalElements; int totalPages; boolean last;
//    static <T> PageResponse<T> of(Page<T> p){
//        return PageResponse.<T>builder().content(p.getContent()).page(p.getNumber()).size(p.getSize())
//            .totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).last(p.isLast()).build();
//    }
//}
