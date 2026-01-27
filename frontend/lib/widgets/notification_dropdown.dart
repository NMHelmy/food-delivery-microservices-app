import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/notification_provider.dart';
import 'notification_item.dart';

class NotificationDropdown extends StatefulWidget {
  const NotificationDropdown({Key? key}) : super(key: key);

  @override
  State<NotificationDropdown> createState() => _NotificationDropdownState();
}

class _NotificationDropdownState extends State<NotificationDropdown> {
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();

    // Fetch notifications when dropdown opens
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final provider = Provider.of<NotificationProvider>(context, listen: false);
      if (provider.notifications.isEmpty) {
        provider.fetchNotifications();
      }
    });

    // Listen for scroll to implement pagination
    _scrollController.addListener(_onScroll);
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent * 0.9) {
      // User scrolled near bottom, load more
      final provider = Provider.of<NotificationProvider>(context, listen: false);
      provider.loadMoreNotifications();
    }
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      height: MediaQuery.of(context).size.height * 0.8,
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      child: Column(
        children: [
          _buildHeader(context),
          const Divider(height: 1),
          Expanded(
            child: _buildNotificationList(),
          ),
        ],
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    return Consumer<NotificationProvider>(
      builder: (context, provider, child) {
        return Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Notifications',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Row(
                children: [
                  if (provider.unreadCount > 0)
                    TextButton(
                      onPressed: () async {
                        await provider.markAllAsRead();
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('All notifications marked as read'),
                              duration: Duration(seconds: 2),
                            ),
                          );
                        }
                      },
                      child: const Text('Mark all read'),
                    ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildNotificationList() {
    return Consumer<NotificationProvider>(
      builder: (context, provider, child) {
        if (provider.isLoading && provider.notifications.isEmpty) {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }

        if (provider.hasError) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(
                  Icons.error_outline,
                  size: 48,
                  color: Colors.red,
                ),
                const SizedBox(height: 16),
                Text(
                  provider.errorMessage ?? 'Failed to load notifications',
                  textAlign: TextAlign.center,
                  style: const TextStyle(color: Colors.red),
                ),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: () => provider.fetchNotifications(),
                  child: const Text('Retry'),
                ),
              ],
            ),
          );
        }

        if (provider.notifications.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  Icons.notifications_none,
                  size: 64,
                  color: Colors.grey[400],
                ),
                const SizedBox(height: 16),
                Text(
                  'No notifications yet',
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          );
        }

        return RefreshIndicator(
          onRefresh: provider.refreshNotifications,
          child: ListView.builder(
            controller: _scrollController,
            physics: const AlwaysScrollableScrollPhysics(),
            itemCount: provider.notifications.length +
                (provider.hasNextPage ? 1 : 0),
            itemBuilder: (context, index) {
              // Loading indicator at the end
              if (index == provider.notifications.length) {
                return const Padding(
                  padding: EdgeInsets.all(16),
                  child: Center(
                    child: CircularProgressIndicator(),
                  ),
                );
              }

              final notification = provider.notifications[index];
              return NotificationItem(
                notification: notification,
                onTap: () async {
                  if (!notification.isRead) {
                    await provider.markAsRead(notification.id);
                  }
                  if (context.mounted) {
                    _handleNotificationTap(context, notification);
                  }
                },
              );
            },
          ),
        );
      },
    );
  }

  void _handleNotificationTap(BuildContext context, notification) {
    // Navigate based on notification type and IDs
    Navigator.pop(context); // Close dropdown first

    // Example navigation logic - customize based on your app's routes
    if (notification.orderId != null) {
      // Navigate to order details
      // Navigator.pushNamed(context, '/order-details', arguments: notification.orderId);
      debugPrint('Navigate to order: ${notification.orderId}');
    } else if (notification.paymentId != null) {
      // Navigate to payment details
      debugPrint('Navigate to payment: ${notification.paymentId}');
    } else if (notification.deliveryId != null) {
      // Navigate to delivery tracking
      debugPrint('Navigate to delivery: ${notification.deliveryId}');
    }
  }
}