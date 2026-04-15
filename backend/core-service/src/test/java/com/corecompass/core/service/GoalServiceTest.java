package com.corecompass.core.service;

import com.corecompass.core.dto.GoalRequest;
import com.corecompass.core.dto.GoalResponse;
import com.corecompass.core.entity.GoalEntity;
import com.corecompass.core.entity.GoalTypeEntity;
import com.corecompass.core.exception.GoalNotFoundException;
import com.corecompass.core.repository.GoalRepository;
import com.corecompass.core.repository.GoalTypeRepository;
import com.corecompass.core.repository.MilestoneRepository;
import com.corecompass.core.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalService Unit Tests")
class GoalServiceTest {

    @Mock GoalRepository      goalRepository;
    @Mock GoalTypeRepository  goalTypeRepository;
    @Mock TodoRepository      todoRepository;
    @Mock MilestoneRepository milestoneRepository;
    @Mock GoogleCalendarService calendarService;

    @InjectMocks GoalService goalService;

    UUID userId;
    UUID categoryTypeId;
    GoalTypeEntity mockType;

    @BeforeEach
    void setUp() {
        userId         = UUID.randomUUID();
        categoryTypeId = UUID.randomUUID();
        mockType = GoalTypeEntity.builder()
            .id(categoryTypeId).name("FITNESS").icon("🏃").color("#FF6B35").isSystem(true).build();
    }

    @Test
    @DisplayName("createGoal: saves goal and returns response with correct title")
    void createGoal_success() {
        GoalRequest req = new GoalRequest();
        req.setTitle("Run 5km by July");
        req.setCategoryTypeId(categoryTypeId);
        req.setTargetDate(LocalDate.now().plusMonths(3));

        when(goalTypeRepository.findById(categoryTypeId)).thenReturn(Optional.of(mockType));
        when(goalRepository.save(any(GoalEntity.class))).thenAnswer(inv -> {
            GoalEntity g = inv.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(g, "id", UUID.randomUUID());
            return g;
        });
        when(todoRepository.countByGoalIdAndIsDeletedFalse(any())).thenReturn(0L);
        when(todoRepository.countByGoalIdAndCompletedTrueAndIsDeletedFalse(any())).thenReturn(0L);
        when(milestoneRepository.findByGoalIdOrderByTargetDateAsc(any())).thenReturn(java.util.List.of());
        when(goalTypeRepository.findById(categoryTypeId)).thenReturn(Optional.of(mockType));

        GoalResponse result = goalService.createGoal(userId, req);

        assertThat(result.getTitle()).isEqualTo("Run 5km by July");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getProgressPct()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(goalRepository).save(any(GoalEntity.class));
    }

    @Test
    @DisplayName("getGoal: throws GoalNotFoundException when goal not owned by user")
    void getGoal_notOwned_throws() {
        UUID goalId = UUID.randomUUID();
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.getGoal(userId, goalId))
            .isInstanceOf(GoalNotFoundException.class)
            .hasMessageContaining(goalId.toString());
    }

    @Test
    @DisplayName("deleteGoal: sets isDeleted=true (soft delete)")
    void deleteGoal_softDelete() {
        UUID goalId = UUID.randomUUID();
        GoalEntity goal = GoalEntity.builder()
            .id(goalId).userId(userId).title("Test").categoryTypeId(categoryTypeId)
            .targetDate(LocalDate.now().plusDays(30)).status("ACTIVE").createdBy(userId).build();

        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenReturn(goal);

        goalService.deleteGoal(userId, goalId);

        assertThat(goal.isDeleted()).isTrue();
        verify(goalRepository).save(goal);
    }

    @Test
    @DisplayName("archiveGoal: sets status to ARCHIVED")
    void archiveGoal_setsStatus() {
        UUID goalId = UUID.randomUUID();
        GoalEntity goal = GoalEntity.builder()
            .id(goalId).userId(userId).title("Test").categoryTypeId(categoryTypeId)
            .targetDate(LocalDate.now().plusDays(30)).status("ACTIVE").createdBy(userId).build();

        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenReturn(goal);
        when(todoRepository.countByGoalIdAndIsDeletedFalse(any())).thenReturn(0L);
        when(todoRepository.countByGoalIdAndCompletedTrueAndIsDeletedFalse(any())).thenReturn(0L);
        when(milestoneRepository.findByGoalIdOrderByTargetDateAsc(any())).thenReturn(java.util.List.of());
        when(goalTypeRepository.findById(any())).thenReturn(Optional.of(mockType));

        GoalResponse result = goalService.archiveGoal(userId, goalId);

        assertThat(result.getStatus()).isEqualTo("ARCHIVED");
    }
}
