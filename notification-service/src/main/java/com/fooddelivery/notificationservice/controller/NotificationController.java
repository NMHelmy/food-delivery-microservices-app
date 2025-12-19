package com.fooddelivery.notificationservice.controller;

import com.fooddelivery.notificationservice.dto.NotificationResponse;
import com.fooddelivery.notificationservice.dto.SendNotificationRequest;
import com.fooddelivery.notificationservice.exception.UnauthorizedException;
import com.fooddelivery.notificationservice.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
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

    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            throw new UnauthorizedException("User ID not found in request headers");
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid User ID format");
        }
    }

    private String getUserRoleFromHeader(HttpServletRequest request) {
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader == null || roleHeader.isEmpty()) {
            throw new UnauthorizedException("User role not found in request headers");
        }
        return roleHeader;
    }

    /**
     * GET /notifications - Get my notifications (paginated)
     * Token required: Any authenticated user
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        Page<NotificationResponse> notifications = notificationService
                .getNotificationsByUserId(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /notifications/unread - Get unread notification count
     * Token required: Any authenticated user
     */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        Long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /notifications/{id}/read - Mark notification as read
     * Token required: Any authenticated user
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        NotificationResponse notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /notifications/read-all - Mark all notifications as read
     * Token required: Any authenticated user
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        notificationService.markAllAsRead(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /notifications/send - Manually send notification (Admin/Testing)
     * Token required: ADMIN
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            HttpServletRequest httpRequest) {
        String role = getUserRoleFromHeader(httpRequest);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admins can send manual notifications");
        }

        NotificationResponse notification = notificationService.createNotification(
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
