import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/owner_service.dart';

class OwnerRestaurantOrdersScreen extends StatefulWidget {
  final int restaurantId;
  final String restaurantName;

  const OwnerRestaurantOrdersScreen({
    super.key,
    required this.restaurantId,
    required this.restaurantName,
  });

  @override
  State<OwnerRestaurantOrdersScreen> createState() => _OwnerRestaurantOrdersScreenState();
}

class _OwnerRestaurantOrdersScreenState extends State<OwnerRestaurantOrdersScreen> {
  late Future<List<dynamic>> _ordersFuture;
  late Future<List<dynamic>> _deliveriesFuture;

  @override
  void initState() {
    super.initState();
    _ordersFuture = OwnerService.getRestaurantOrders(widget.restaurantId);
    _deliveriesFuture = OwnerService.getMyRestaurantDeliveries();
  }

  Future<void> _refresh() async {
    setState(() {
      _ordersFuture = OwnerService.getRestaurantOrders(widget.restaurantId);
      _deliveriesFuture = OwnerService.getMyRestaurantDeliveries();
    });
    await Future.wait([_ordersFuture, _deliveriesFuture]);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Orders • ${widget.restaurantName}",
            style: const TextStyle(fontWeight: FontWeight.w900)),
        centerTitle: true,
      ),
      body: RefreshIndicator(
        onRefresh: _refresh,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          children: [
            FutureBuilder<List<dynamic>>(
              future: Future.wait([_ordersFuture, _deliveriesFuture]).then((v) => v[0] as List<dynamic>),
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) {
                  return _loadingBox(height: 220);
                }
                if (snap.hasError) {
                  return _errorCard("Failed to load orders", snap.error.toString());
                }

                // orders are in snap.data, deliveries are still available in _deliveriesFuture via separate builder:
                return FutureBuilder<List<dynamic>>(
                  future: _deliveriesFuture,
                  builder: (context, dSnap) {
                    if (dSnap.connectionState == ConnectionState.waiting) {
                      return _loadingBox(height: 220);
                    }
                    if (dSnap.hasError) {
                      return _errorCard("Failed to load deliveries", dSnap.error.toString());
                    }

                    final orders = (snap.data ?? []);
                    final deliveries = (dSnap.data ?? []);

                    final existingDeliveryOrderIds = deliveries
                        .map((e) => ((e as Map)['orderId']))
                        .where((x) => x != null)
                        .toSet();

                    if (orders.isEmpty) {
                      return _empty("No orders", "Orders will appear here.");
                    }

                    return Column(
                      children: orders.map((raw) {
                        final o = (raw as Map).cast<String, dynamic>();
                        final orderId = (o['id'] as num).toInt();
                        final status = (o['status'] ?? '-').toString();
                        final total = o['total'];
                        final customerId = (o['customerId'] as num).toInt();
                        final deliveryAddressId = (o['deliveryAddressId'] as num).toInt();
                        final est = (o['estimatedDeliveryTime'] ?? '').toString();
                        final special = (o['specialInstructions'] ?? '').toString();
                        final items = (o['items'] as List? ?? []);

                        final hasDelivery = existingDeliveryOrderIds.contains(orderId);

                        return Container(
                          margin: const EdgeInsets.only(bottom: 12),
                          padding: const EdgeInsets.all(14),
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
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text("Order $orderId",
                                        style: const TextStyle(
                                            fontWeight: FontWeight.w900, fontSize: 16)),
                                  ),
                                  _pill(text: status, color: _statusColor(status)),
                                ],
                              ),
                              const SizedBox(height: 6),
                              Text("Total: $total",
                                  style: TextStyle(
                                      color: Colors.grey[700], fontWeight: FontWeight.w800)),
                              if (est.isNotEmpty) ...[
                                const SizedBox(height: 4),
                                Text("ETA: $est", style: TextStyle(color: Colors.grey[700])),
                              ],
                              if (special.trim().isNotEmpty) ...[
                                const SizedBox(height: 6),
                                Text("Notes: $special", style: TextStyle(color: Colors.grey[800])),
                              ],
                              const SizedBox(height: 10),
                              ...items.map((itRaw) {
                                final it = (itRaw as Map).cast<String, dynamic>();
                                final itemName = (it['itemName'] ?? '').toString();
                                final qty = it['quantity'];
                                final price = it['price'];
                                return Padding(
                                  padding: const EdgeInsets.only(bottom: 4),
                                  child: Text("• $itemName × $qty  ($price)",
                                      style: TextStyle(color: Colors.grey[800])),
                                );
                              }).toList(),
                              const SizedBox(height: 12),

                              // Update status
                              Row(
                                children: [
                                  Expanded(
                                    child: DropdownButtonFormField<String>(
                                      value: status,
                                      decoration: InputDecoration(
                                        labelText: "Update status",
                                        border: OutlineInputBorder(
                                            borderRadius: BorderRadius.circular(14)),
                                      ),
                                      items: const [
                                        DropdownMenuItem(value: "PENDING", child: Text("PENDING")),
                                        DropdownMenuItem(value: "ACCEPTED", child: Text("ACCEPTED")),
                                        DropdownMenuItem(value: "PREPARING", child: Text("PREPARING")),
                                        DropdownMenuItem(value: "READY_FOR_PICKUP", child: Text("READY_FOR_PICKUP")),
                                        DropdownMenuItem(value: "COMPLETED", child: Text("COMPLETED")),
                                        DropdownMenuItem(value: "CANCELLED", child: Text("CANCELLED")),
                                      ],
                                      onChanged: (newStatus) async {
                                        if (newStatus == null || newStatus == status) return;
                                        try {
                                          await OwnerService.updateOrderStatus(orderId, newStatus);
                                          await _refresh();
                                        } catch (e) {
                                          if (!context.mounted) return;
                                          _snack(context, e.toString(), isError: true);
                                        }
                                      },
                                    ),
                                  ),
                                ],
                              ),

                              const SizedBox(height: 12),
                              SizedBox(
                                height: 44,
                                width: double.infinity,
                                child: ElevatedButton.icon(
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor:
                                    hasDelivery ? Colors.grey[400] : AppTheme.primaryOrange,
                                    foregroundColor: Colors.white,
                                    shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.circular(14)),
                                  ),
                                  onPressed: hasDelivery
                                      ? null
                                      : () async {
                                    try {
                                      await OwnerService.createDelivery(
                                        orderId: orderId,
                                        restaurantId: widget.restaurantId,
                                        customerId: customerId,
                                        deliveryAddressId: deliveryAddressId,
                                        deliveryNotes:
                                        special.trim().isEmpty ? null : special.trim(),
                                        estimatedDeliveryTime: est.isEmpty ? null : est,
                                      );
                                      await _refresh();
                                    } catch (e) {
                                      if (!context.mounted) return;
                                      _snack(context, e.toString(), isError: true);
                                    }
                                  },
                                  icon: const Icon(Icons.local_shipping_rounded, size: 18),
                                  label: Text(hasDelivery ? "Delivery already created" : "Create delivery",
                                      style: const TextStyle(fontWeight: FontWeight.w900)),
                                ),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                    );
                  },
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  static Widget _pill({required String text, required Color color}) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
    decoration: BoxDecoration(
      color: color.withOpacity(0.12),
      borderRadius: BorderRadius.circular(14),
    ),
    child: Text(text,
        style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: 12)),
  );

  static Color _statusColor(String s) {
    switch (s) {
      case "COMPLETED":
        return const Color(0xFF2E7D32);
      case "CANCELLED":
        return const Color(0xFFC62828);
      case "READY_FOR_PICKUP":
        return const Color(0xFF1565C0);
      default:
        return const Color(0xFFF57C00);
    }
  }

  static Widget _loadingBox({required double height}) => Container(
    height: height,
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: const Center(child: CircularProgressIndicator()),
  );

  static Widget _errorCard(String title, String message) => Container(
    padding: const EdgeInsets.all(16),
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
        const SizedBox(height: 8),
        Text(message, style: const TextStyle(color: Colors.red)),
      ],
    ),
  );

  static Widget _empty(String title, String subtitle) => Container(
    padding: const EdgeInsets.all(18),
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: Column(
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
        const SizedBox(height: 6),
        Text(subtitle, textAlign: TextAlign.center, style: TextStyle(color: Colors.grey[700])),
      ],
    ),
  );

  static void _snack(BuildContext context, String text, {bool isError = false}) {
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
