package com.corecompass.core.service;

import com.corecompass.core.entity.TodoEntity;
import com.corecompass.core.repository.TodoRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Asynchronous Google Calendar sync service.
 *
 * Per LLD Section 5.3:
 *  - createTodo() fires @Async → user gets instant 201
 *  - Calendar sync happens in background thread
 *  - On failure: retry up to 3 times with exponential backoff
 *  - All retries fail → log ERROR, store failed_calendar_syncs record
 *
 * Uses Service Account credentials (no per-user OAuth needed).
 * User grants Core Compass SA access to their calendar once during setup.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final TodoRepository todoRepository;

    @Value("${google.calendar.enabled:false}")
    private boolean calendarEnabled;

    @Value("${google.calendar.timezone:Asia/Kolkata}")
    private String timezone;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Fires after todo is saved — non-blocking.
     * User already received the 201 response.
     */
    @Async("calendarTaskExecutor")
    public void syncToCalendar(TodoEntity todo, String goalTitle) {
        if (!calendarEnabled) {
            log.debug("Google Calendar sync disabled — skipping for todoId={}", todo.getId());
            return;
        }
        log.info("Starting calendar sync for todoId={}", todo.getId());
        doSync(todo, goalTitle, 1);
    }

    private void doSync(TodoEntity todo, String goalTitle, int attempt) {
        try {
            Calendar calendarService = buildCalendarService();

            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime start = ZonedDateTime.of(
                todo.getDueDate(),
                todo.getDueTime(),
                zoneId
            );
            ZonedDateTime end = start.plusHours(1); // Default 1-hour event

            Event event = new Event()
                .setSummary("[CoreCompass] " + todo.getTitle())
                .setDescription("Goal: " + goalTitle + "\n\nManaged by CoreCompass")
                .setStart(new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(
                        java.util.Date.from(start.toInstant())
                    ))
                    .setTimeZone(timezone)
                )
                .setEnd(new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(
                        java.util.Date.from(end.toInstant())
                    ))
                    .setTimeZone(timezone)
                );

            Event created = calendarService.events()
                .insert("primary", event)
                .execute();

            // Update todo with calendar event ID
            todoRepository.findById(todo.getId()).ifPresent(t -> {
                t.setCalendarEventId(created.getId());
                todoRepository.save(t);
            });

            log.info("Calendar event created: eventId={} todoId={}", created.getId(), todo.getId());

        } catch (Exception ex) {
            log.warn("Calendar sync attempt {}/{} failed for todoId={}: {}",
                attempt, MAX_RETRY_ATTEMPTS, todo.getId(), ex.getMessage());

            if (attempt < MAX_RETRY_ATTEMPTS) {
                // Exponential backoff: 500ms, 1s, 2s
                long delay = (long) (500 * Math.pow(2, attempt - 1));
                try { Thread.sleep(delay); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                doSync(todo, goalTitle, attempt + 1);
            } else {
                log.error("All {} calendar sync attempts failed for todoId={}. Manual retry needed.",
                    MAX_RETRY_ATTEMPTS, todo.getId());
                // Increment attempt counter so retry cron won't re-queue it endlessly
                todoRepository.findById(todo.getId()).ifPresent(t -> {
                    t.setCalendarSyncAttempts(t.getCalendarSyncAttempts() + 1);
                    todoRepository.save(t);
                });
            }
        }
    }

    /**
     * Retry cron — runs every 30 minutes, picks up todos that missed sync.
     * Handles cases where the service restarted before async could complete.
     */
    @Scheduled(fixedDelay = 1800000)  // 30 minutes
    @Transactional
    public void retryFailedSyncs() {
        if (!calendarEnabled) return;
        List<TodoEntity> pending = todoRepository.findPendingCalendarSync();
        if (!pending.isEmpty()) {
            log.info("Retry cron: {} todos pending calendar sync", pending.size());
            pending.forEach(todo -> syncToCalendar(todo, "Goal"));
        }
    }

    private Calendar buildCalendarService() throws Exception {
        // In production: load service account credentials from env/secret
        // GoogleCredentials credentials = GoogleCredentials
        //     .fromStream(new FileInputStream(credentialsPath))
        //     .createScoped(List.of(CalendarScopes.CALENDAR));

        // Fallback for dev: application default credentials
        GoogleCredentials credentials = GoogleCredentials
            .getApplicationDefault()
            .createScoped(List.of("https://www.googleapis.com/auth/calendar"));

        return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials)
        ).setApplicationName("CoreCompass").build();
    }
}
