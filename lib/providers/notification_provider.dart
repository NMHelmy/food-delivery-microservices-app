import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/notification.dart';
import '../services/notification_service.dart';

class NotificationProvider with ChangeNotifier {
  List<NotificationModel> _notifications = [];
  int _unreadCount = 0;
  bool _isLoading = false;
  bool _hasError = false;
  String? _errorMessage;
  Timer? _pollingTimer;

  // Pagination
  int _currentPage = 0;
  int _totalPages = 0;
  bool _hasNextPage = false;

  // Getters
  List<NotificationModel> get notifications => _notifications;
  int get unreadCount => _unreadCount;
  bool get isLoading => _isLoading;
  bool get hasError => _hasError;
  String? get errorMessage => _errorMessage;
  bool get hasNextPage => _hasNextPage;
  int get currentPage => _currentPage;

  /// Initialize notification polling
  /// Polls every 30 seconds by default
  void startPolling({Duration interval = const Duration(seconds: 30)}) {
    stopPolling(); // Stop existing timer if any

    // Fetch immediately
    fetchNotifications();
    fetchUnreadCount();

    // Then poll at intervals
    _pollingTimer = Timer.periodic(interval, (_) {
      fetchUnreadCount(); // Only poll unread count for efficiency
    });
  }

  /// Stop notification polling
  void stopPolling() {
    _pollingTimer?.cancel();
    _pollingTimer = null;
  }

  /// Fetch notifications (paginated)
  Future<void> fetchNotifications({int page = 0, int size = 20}) async {
    if (_isLoading) return;

    _isLoading = true;
    _hasError = false;
    _errorMessage = null;
    notifyListeners();

    try {
      final result = await NotificationService.getMyNotifications(
        page: page,
        size: size,
      );

      if (page == 0) {
        // First page - replace all
        _notifications = result['notifications'] as List<NotificationModel>;
      } else {
        // Subsequent pages - append
        _notifications.addAll(result['notifications'] as List<NotificationModel>);
      }

      _currentPage = result['currentPage'] as int;
      _totalPages = result['totalPages'] as int;
      _hasNextPage = result['hasNext'] as bool;

      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _hasError = true;
      _errorMessage = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Load next page of notifications
  Future<void> loadMoreNotifications() async {
    if (_hasNextPage && !_isLoading) {
      await fetchNotifications(page: _currentPage + 1);
    }
  }

  /// Refresh notifications (pull-to-refresh)
  Future<void> refreshNotifications() async {
    await fetchNotifications(page: 0);
    await fetchUnreadCount();
  }

  /// Fetch unread count only
  Future<void> fetchUnreadCount() async {
    try {
      final count = await NotificationService.getUnreadCount();
      _unreadCount = count;
      notifyListeners();
    } catch (e) {
      // Silently fail for background polling
      debugPrint("Failed to fetch unread count: $e");
    }
  }

  /// Mark a notification as read
  Future<void> markAsRead(int notificationId) async {
    try {
      final updatedNotification = await NotificationService.markAsRead(notificationId);

      // Update local state
      final index = _notifications.indexWhere((n) => n.id == notificationId);
      if (index != -1) {
        _notifications[index] = updatedNotification;
        if (!updatedNotification.isRead) {
          _unreadCount = (_unreadCount - 1).clamp(0, double.infinity).toInt();
        }
        notifyListeners();
      }
    } catch (e) {
      _hasError = true;
      _errorMessage = e.toString();
      notifyListeners();
      rethrow;
    }
  }

  /// Mark all notifications as read
  Future<void> markAllAsRead() async {
    try {
      await NotificationService.markAllAsRead();

      // Update local state
      _notifications = _notifications.map((n) {
        return n.copyWith(isRead: true, readAt: DateTime.now());
      }).toList();

      _unreadCount = 0;
      notifyListeners();
    } catch (e) {
      _hasError = true;
      _errorMessage = e.toString();
      notifyListeners();
      rethrow;
    }
  }

  /// Clear error state
  void clearError() {
    _hasError = false;
    _errorMessage = null;
    notifyListeners();
  }

  @override
  void dispose() {
    stopPolling();
    super.dispose();
  }
}