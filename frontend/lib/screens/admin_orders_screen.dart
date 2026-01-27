import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/admin_service.dart';

class AdminOrdersScreen extends StatefulWidget {
  const AdminOrdersScreen({super.key});

  @override
  State<AdminOrdersScreen> createState() => _AdminOrdersScreenState();
}

class _AdminOrdersScreenState extends State<AdminOrdersScreen> {
  List<dynamic> _allOrders = [];
  List<dynamic> _filteredOrders = [];

  String _searchQuery = "";
  String? _statusFilter;
  bool _loadingAction = false;

  final List<String> _orderStatuses = [
    'PENDING',
    'CONFIRMED',
    'PREPARING',
    'READY_FOR_PICKUP',
    'PICKED_UP',
    'DELIVERED',
    'CANCELLED',
  ];

  final List<String> _paymentStatuses = [
    'PENDING',
    'CONFIRMED',
    'FAILED',
    'REFUNDED',
  ];

  @override
  void initState() {
    super.initState();
    _loadOrders();
  }

  Future<void> _loadOrders() async {
    try {
      final orders = await AdminService.getOrders();
      print("DEBUG: Loaded ${orders.length} orders"); // Debug
      print("DEBUG: First order: ${orders.isNotEmpty ? orders[0] : 'none'}"); // Debug
      setState(() {
        _allOrders = orders;
        _filteredOrders = orders; // Set filtered to all initially
      });
      print("DEBUG: _allOrders length = ${_allOrders.length}"); // Debug
      print("DEBUG: _filteredOrders length = ${_filteredOrders.length}"); // Debug

      if (orders.isEmpty) {
        _snack("No orders in database", isError: false);
      } else {
        _snack("Loaded ${orders.length} orders", isError: false);
      }
    } catch (e) {
      print("DEBUG: Error loading orders: $e"); // Debug
      _snack("Error loading orders: $e", isError: true);
    }
  }

  void _applyFilters() {
    setState(() {
      _filteredOrders = _allOrders.where((order) {
        final o = (order as Map).cast<String, dynamic>();

        // Status filter
        if (_statusFilter != null) {
          final status = (o["status"] ?? "").toString().toUpperCase();
          if (status != _statusFilter) return false;
        }

        // Search query (order ID, restaurant name, customer info)
        if (_searchQuery.isNotEmpty) {
          final id = (o["id"] ?? "").toString().toLowerCase();
          final restaurant = (o["restaurantName"] ?? "").toString().toLowerCase();
          final customerId = (o["customerId"] ?? "").toString().toLowerCase();

          final query = _searchQuery.toLowerCase();
          if (!id.contains(query) &&
              !restaurant.contains(query) &&
              !customerId.contains(query)) {
            return false;
          }
        }

        return true;
      }).toList();
    });
  }

  Future<void> _updateStatus(int orderId, String currentStatus) async {
    final status = await showDialog<String>(
      context: context,
      builder: (context) => _StatusDialog(
        title: "Update Order Status",
        currentValue: currentStatus,
        options: _orderStatuses,
      ),
    );

    if (status == null || status.isEmpty) return;

    setState(() => _loadingAction = true);
    try {
      await AdminService.updateOrderStatus(orderId, status);
      _snack("Order #$orderId status → $status");
      await _loadOrders();
    } catch (e) {
      _snack("Failed: ${e.toString()}", isError: true);
    } finally {
      if (mounted) setState(() => _loadingAction = false);
    }
  }

  Future<void> _updatePayment(int orderId, String currentPayment) async {
    final payment = await showDialog<String>(
      context: context,
      builder: (context) => _StatusDialog(
        title: "Update Payment Status",
        currentValue: currentPayment,
        options: _paymentStatuses,
      ),
    );

    if (payment == null || payment.isEmpty) return;

    setState(() => _loadingAction = true);
    try {
      await AdminService.updateOrderPayment(orderId, payment);
      _snack("Order #$orderId payment → $payment");
      await _loadOrders();
    } catch (e) {
      _snack("Failed: ${e.toString()}", isError: true);
    } finally {
      if (mounted) setState(() => _loadingAction = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Orders", style: TextStyle(fontWeight: FontWeight.w900)),
        actions: [
          IconButton(
            onPressed: _loadingAction ? null : _loadOrders,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: Column(
        children: [
          // Search bar
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
            child: TextField(
              onChanged: (value) {
                _searchQuery = value;
                _applyFilters();
              },
              decoration: InputDecoration(
                hintText: "Search by order ID, restaurant, customer...",
                prefixIcon: const Icon(Icons.search),
                filled: true,
                fillColor: Colors.white,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(30),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
          ),

          // Status filter chips
          SizedBox(
            height: 50,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              children: [
                _FilterChip(
                  label: "All",
                  isSelected: _statusFilter == null,
                  onTap: () {
                    setState(() => _statusFilter = null);
                    _applyFilters();
                  },
                ),
                ..._orderStatuses.map((status) => _FilterChip(
                  label: status,
                  isSelected: _statusFilter == status,
                  onTap: () {
                    setState(() => _statusFilter = status);
                    _applyFilters();
                  },
                )),
              ],
            ),
          ),

          // Results count
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: [
                Text(
                  "${_filteredOrders.length} orders",
                  style: TextStyle(
                    fontWeight: FontWeight.w700,
                    color: Colors.grey[700],
                  ),
                ),
              ],
            ),
          ),

          // Orders list
          Expanded(
            child: _allOrders.isEmpty && _filteredOrders.isEmpty
                ? const Center(child: CircularProgressIndicator())
                : _filteredOrders.isEmpty
                ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.inbox_outlined, size: 64, color: Colors.grey[400]),
                  const SizedBox(height: 16),
                  Text(
                    "No orders match filters",
                    style: TextStyle(
                      fontWeight: FontWeight.w900,
                      color: Colors.grey[700],
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: () {
                      setState(() {
                        _statusFilter = null;
                        _searchQuery = "";
                        _filteredOrders = _allOrders;
                      });
                    },
                    child: const Text("Clear filters"),
                  ),
                ],
              ),
            )
                : RefreshIndicator(
              onRefresh: _loadOrders,
              child: ListView.builder(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                itemCount: _filteredOrders.length,
                itemBuilder: (context, i) {
                  final o = (_filteredOrders[i] as Map).cast<String, dynamic>();
                  final id = (o["id"] as num).toInt();
                  final status = (o["status"] ?? "UNKNOWN").toString();
                  final payment = (o["paymentStatus"] ?? "UNKNOWN").toString();
                  final restaurantName = (o["restaurantName"] ?? "Restaurant").toString();
                  final total = o["total"];
                  final customerId = o["customerId"];

                  return Container(
                    margin: const EdgeInsets.only(bottom: 12),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 10,
                          offset: const Offset(0, 6),
                        )
                      ],
                    ),
                    child: Column(
                      children: [
                        Padding(
                          padding: const EdgeInsets.all(14),
                          child: Row(
                            children: [
                              Container(
                                width: 46,
                                height: 46,
                                decoration: BoxDecoration(
                                  color: AppTheme.primaryOrange.withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(14),
                                ),
                                child: const Icon(
                                  Icons.receipt_long_rounded,
                                  color: AppTheme.primaryOrange,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      "Order #$id",
                                      style: const TextStyle(fontWeight: FontWeight.w900),
                                    ),
                                    const SizedBox(height: 4),
                                    Text(
                                      restaurantName,
                                      style: TextStyle(
                                        color: Colors.grey[700],
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                    const SizedBox(height: 4),
                                    Text(
                                      "Customer #$customerId",
                                      style: TextStyle(
                                        color: Colors.grey[600],
                                        fontSize: 12,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.end,
                                children: [
                                  Text(
                                    "${total ?? '-'} EGP",
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w900,
                                      fontSize: 16,
                                    ),
                                  ),
                                  const SizedBox(height: 8),
                                  _pill(text: status, color: _statusColor(status)),
                                ],
                              ),
                            ],
                          ),
                        ),
                        const Divider(height: 1),
                        Padding(
                          padding: const EdgeInsets.all(10),
                          child: Row(
                            children: [
                              _pill(text: "💳 $payment", color: _paymentColor(payment)),
                              const Spacer(),
                              TextButton.icon(
                                onPressed: _loadingAction ? null : () => _updateStatus(id, status),
                                icon: const Icon(Icons.edit, size: 16),
                                label: const Text("Status"),
                              ),
                              TextButton.icon(
                                onPressed: _loadingAction ? null : () => _updatePayment(id, payment),
                                icon: const Icon(Icons.payment, size: 16),
                                label: const Text("Payment"),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }

  static Widget _pill({required String text, required Color color}) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
    decoration: BoxDecoration(
      color: color.withOpacity(0.12),
      borderRadius: BorderRadius.circular(14),
    ),
    child: Text(
      text,
      style: TextStyle(
        color: color,
        fontWeight: FontWeight.w900,
        fontSize: 12,
      ),
    ),
  );

  static Color _statusColor(String s) {
    switch (s.toUpperCase()) {
      case "DELIVERED":
      case "COMPLETED":
        return const Color(0xFF2E7D32);
      case "CANCELLED":
        return const Color(0xFFC62828);
      case "PREPARING":
      case "READY":
        return const Color(0xFFF57C00);
      default:
        return const Color(0xFF1565C0);
    }
  }

  static Color _paymentColor(String s) {
    switch (s.toUpperCase()) {
      case "PAID":
        return const Color(0xFF2E7D32);
      case "PENDING":
        return const Color(0xFFF57C00);
      case "FAILED":
      case "REFUNDED":
        return const Color(0xFFC62828);
      default:
        return const Color(0xFF1565C0);
    }
  }

  void _snack(String text, {bool isError = false}) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).clearSnackBars();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        behavior: SnackBarBehavior.floating,
        backgroundColor: isError ? const Color(0xFFC62828) : const Color(0xFF2D2D2D),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        content: Text(
          text,
          maxLines: 3,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(fontWeight: FontWeight.w800),
        ),
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  const _FilterChip({
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          decoration: BoxDecoration(
            color: isSelected ? AppTheme.primaryOrange : Colors.white,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: isSelected ? AppTheme.primaryOrange : Colors.grey[300]!,
            ),
          ),
          child: Text(
            label,
            style: TextStyle(
              color: isSelected ? Colors.white : Colors.grey[700],
              fontWeight: FontWeight.w700,
              fontSize: 13,
            ),
          ),
        ),
      ),
    );
  }
}

class _StatusDialog extends StatelessWidget {
  final String title;
  final String currentValue;
  final List<String> options;

  const _StatusDialog({
    required this.title,
    required this.currentValue,
    required this.options,
  });

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(title),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: options.map((status) {
          final isSelected = status.toUpperCase() == currentValue.toUpperCase();
          return ListTile(
            title: Text(status),
            leading: Icon(
              isSelected ? Icons.radio_button_checked : Icons.radio_button_off,
              color: isSelected ? AppTheme.primaryOrange : Colors.grey,
            ),
            onTap: () => Navigator.pop(context, status),
          );
        }).toList(),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text("Cancel"),
        ),
      ],
    );
  }
}