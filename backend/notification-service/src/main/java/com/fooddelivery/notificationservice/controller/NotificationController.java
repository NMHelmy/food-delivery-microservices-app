package com.fooddelivery.notificationservice.controller;

import com.fooddelivery.notificationservice.dto.NotificationResponse;
import com.fooddelivery.notificationservice.dto.SendNotificationRequest;
import com.fooddelivery.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // AUTHENTICATED USERS

    /**
     * GET /notifications
     * Any authenticated user
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-Id") Long userId) {

        Page<NotificationResponse> notifications =
                notificationService.getNotificationsByUserId(userId, page, size);

        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /notifications/unread
     * Any authenticated user
     */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {

        Long count = notificationService.getUnreadCount(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /notifications/{id}/read
     * Any authenticated user (ownership validated in service)
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        NotificationResponse notification =
                notificationService.markAsRead(id, userId);

        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /notifications/read-all
     * Any authenticated user
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId) {

        notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(
                Map.of("message", "All notifications marked as read")
        );
    }

    // ADMIN

    /**
     * POST /notifications/send
     * ADMIN only (enforced by gateway)
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {

        NotificationResponse notification =
                notificationService.createNotification(
                        request.getUserId(),
                        request.getType(),
                        request.getTitle(),
                        request.getMessage(),
                        request.getOrderId(),
                        request.getPaymentId(),
                        request.getDeliveryId()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
}
