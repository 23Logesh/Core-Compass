package com.corecompass.core.service;

import com.corecompass.core.dto.*;
import com.corecompass.core.entity.*;
import com.corecompass.core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository    notificationRepo;
    private final UserPreferencesRepository prefsRepo;

    // ── GET /notifications ────────────────────────────────────
    public PageResponse<NotificationResponse> listNotifications(UUID userId, Pageable pageable) {
        return PageResponse.of(
                notificationRepo.findByUserId(userId, pageable).map(this::toResponse));
    }

    // ── PATCH /notifications/{id}/read ────────────────────────
    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        NotificationEntity n = notificationRepo.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));
        n.setRead(true);
        return toResponse(notificationRepo.save(n));
    }

    // ── PATCH /notifications/read-all ─────────────────────────
    @Transactional
    public int markAllRead(UUID userId) {
        int count = notificationRepo.markAllRead(userId);
        log.info("Marked {} notifications as read for userId={}", count, userId);
        return count;
    }

    // ── GET /notifications/preferences ───────────────────────
    public UserPreferencesResponse getPreferences(UUID userId) {
        UserPreferencesEntity prefs = prefsRepo.findByUserId(userId)
                .orElseGet(() -> defaultPrefs(userId)); // return defaults if not set yet
        return toPrefsResponse(prefs);
    }

    // ── PUT /notifications/preferences ────────────────────────
    @Transactional
    public UserPreferencesResponse updatePreferences(UUID userId, UserPreferencesRequest req) {
        UserPreferencesEntity prefs = prefsRepo.findByUserId(userId)
                .orElseGet(() -> UserPreferencesEntity.builder().userId(userId).build());

        if (req.getTheme()    != null) prefs.setTheme(req.getTheme());
        if (req.getCurrency() != null) prefs.setCurrency(req.getCurrency());
        if (req.getTimezone() != null) prefs.setTimezone(req.getTimezone());
        if (req.getUnits()    != null) prefs.setUnits(req.getUnits());
        if (req.getWeeklyReport()   != null) prefs.setWeeklyReport(req.getWeeklyReport());
        if (req.getBudgetAlerts()   != null) prefs.setBudgetAlerts(req.getBudgetAlerts());
        if (req.getHabitReminders() != null) prefs.setHabitReminders(req.getHabitReminders());

        return toPrefsResponse(prefsRepo.save(prefs));
    }

    // ── Internal: create a notification (called by other services/schedulers) ──
    @Transactional
    public void createNotification(UUID userId, String type, String title, String message) {
        NotificationEntity n = NotificationEntity.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationRepo.save(n);
        log.debug("Notification created: type={} userId={}", type, userId);
    }

    // ── Private helpers ───────────────────────────────────────

    private UserPreferencesEntity defaultPrefs(UUID userId) {
        // Not persisted — just returns defaults for the response
        return UserPreferencesEntity.builder().userId(userId).build();
    }

    private NotificationResponse toResponse(NotificationEntity e) {
        return NotificationResponse.builder()
                .id(e.getId())
                .type(e.getType())
                .title(e.getTitle())
                .message(e.getMessage())
                .isRead(e.isRead())
                .metadata(e.getMetadata())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private UserPreferencesResponse toPrefsResponse(UserPreferencesEntity e) {
        return UserPreferencesResponse.builder()
                .theme(e.getTheme())
                .currency(e.getCurrency())
                .timezone(e.getTimezone())
                .units(e.getUnits())
                .weeklyReport(e.isWeeklyReport())
                .budgetAlerts(e.isBudgetAlerts())
                .habitReminders(e.isHabitReminders())
                .build();
    }
}