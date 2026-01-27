package com.fooddelivery.notificationservice.dto;

import com.fooddelivery.notificationservice.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Long orderId;
    private Long paymentId;
    private Long deliveryId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
