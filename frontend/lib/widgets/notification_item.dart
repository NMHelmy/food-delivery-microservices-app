import 'package:flutter/material.dart';
import 'package:timeago/timeago.dart' as timeago;
import '../models/notification.dart';

class NotificationItem extends StatelessWidget {
  final NotificationModel notification;
  final VoidCallback? onTap;

  const NotificationItem({
    Key? key,
    required this.notification,
    this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: notification.isRead
              ? Colors.white
              : Colors.blue.shade50,
          border: Border(
            bottom: BorderSide(
              color: Colors.grey.shade200,
              width: 1,
            ),
          ),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildIcon(),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(
                          notification.title,
                          style: TextStyle(
                            fontSize: 15,
                            fontWeight: notification.isRead
                                ? FontWeight.w500
                                : FontWeight.bold,
                          ),
                        ),
                      ),
                      if (!notification.isRead)
                        Container(
                          width: 8,
                          height: 8,
                          decoration: const BoxDecoration(
                            color: Colors.blue,
                            shape: BoxShape.circle,
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    notification.message,
                    style: TextStyle(
                      fontSize: 14,
                      color: Colors.grey.shade700,
                      height: 1.3,
                    ),
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  Row(
                    children: [
                      Icon(
                        Icons.access_time,
                        size: 12,
                        color: Colors.grey.shade500,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        timeago.format(notification.createdAt),
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey.shade500,
                        ),
                      ),
                      if (notification.orderId != null) ...[
                        const SizedBox(width: 8),
                        Text(
                          '• Order #${notification.orderId}',
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey.shade500,
                          ),
                        ),
                      ],
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildIcon() {
    IconData iconData;
    Color iconColor;

    switch (notification.type) {
    // Order Events
      case 'ORDER_CREATED':
        iconData = Icons.receipt_long;
        iconColor = Colors.blue;
        break;
      case 'ORDER_CONFIRMED':
        iconData = Icons.check_circle;
        iconColor = Colors.green;
        break;
      case 'ORDER_READY':
        iconData = Icons.restaurant;
        iconColor = Colors.orange;
        break;
      case 'ORDER_CANCELLED':
        iconData = Icons.cancel;
        iconColor = Colors.red;
        break;

    // Payment Events
      case 'PAYMENT_CONFIRMED':
        iconData = Icons.payment;
        iconColor = Colors.green;
        break;
      case 'PAYMENT_FAILED':
        iconData = Icons.error;
        iconColor = Colors.red;
        break;
      case 'PAYMENT_REFUNDED':
        iconData = Icons.money_off;
        iconColor = Colors.orange;
        break;

    // Delivery Events
      case 'DELIVERY_ASSIGNED':
        iconData = Icons.delivery_dining;
        iconColor = Colors.blue;
        break;
      case 'DELIVERY_PICKED_UP':
        iconData = Icons.local_shipping;
        iconColor = Colors.orange;
        break;
      case 'DELIVERY_DELIVERED':
        iconData = Icons.done_all;
        iconColor = Colors.green;
        break;

      default:
        iconData = Icons.notifications;
        iconColor = Colors.grey;
    }

    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: iconColor.withOpacity(0.1),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Icon(
        iconData,
        color: iconColor,
        size: 24,
      ),
    );
  }
}