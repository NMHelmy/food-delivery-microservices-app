import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/notification.dart';
import 'token_service.dart';
import '../constants.dart';

class NotificationService {
  /// GET /notifications
  /// Fetch paginated notifications for the current user
  static Future<Map<String, dynamic>> getMyNotifications({
    int page = 0,
    int size = 20,
  }) async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/notifications?page=$page&size=$size"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);

      // Parse paginated response
      final List<NotificationModel> notifications = (data['content'] as List)
          .map((json) => NotificationModel.fromJson(json))
          .toList();

      return {
        'notifications': notifications,
        'totalPages': data['totalPages'] ?? 0,
        'totalElements': data['totalElements'] ?? 0,
        'currentPage': data['number'] ?? 0,
        'hasNext': !(data['last'] ?? true),
      };
    } else {
      throw Exception(_extractError(response));
    }
  }

  /// GET /notifications/unread
  /// Get count of unread notifications
  static Future<int> getUnreadCount() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/notifications/unread"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['unreadCount'] as int;
    } else {
      throw Exception(_extractError(response));
    }
  }

  /// PUT /notifications/{id}/read
  /// Mark a specific notification as read
  static Future<NotificationModel> markAsRead(int notificationId) async {
    final token = await TokenService.getToken();

    final response = await http.put(
      Uri.parse("$baseUrl/notifications/$notificationId/read"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return NotificationModel.fromJson(jsonDecode(response.body));
    } else {
      throw Exception(_extractError(response));
    }
  }

  /// PUT /notifications/read-all
  /// Mark all notifications as read
  static Future<void> markAllAsRead() async {
    final token = await TokenService.getToken();

    final response = await http.put(
      Uri.parse("$baseUrl/notifications/read-all"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode != 200) {
      throw Exception(_extractError(response));
    }
  }

  static String _extractError(http.Response response) {
    try {
      final decoded = jsonDecode(response.body);
      if (decoded is Map && decoded['message'] != null) {
        return decoded['message'].toString();
      }
      return response.body.toString();
    } catch (_) {
      return "Request failed (${response.statusCode})";
    }
  }
}