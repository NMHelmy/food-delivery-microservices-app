import 'package:flutter/material.dart';

import '../theme/app_theme.dart';
import '../services/driver_service.dart';
import 'driver_profile_screen.dart';

class DriverHomeScreen extends StatefulWidget {
  const DriverHomeScreen({super.key});

  @override
  State<DriverHomeScreen> createState() => _DriverHomeScreenState();
}

class _DriverHomeScreenState extends State<DriverHomeScreen> {
  late Future<List<dynamic>> _activeFuture;
  late Future<List<dynamic>> _myDeliveriesFuture;

  @override
  void initState() {
    super.initState();
    _activeFuture = DriverService.getActiveDeliveries();
    _myDeliveriesFuture = DriverService.getMyDriverDeliveries();
  }

  Future<void> _refresh() async {
    setState(() {
      _activeFuture = DriverService.getActiveDeliveries();
      _myDeliveriesFuture = DriverService.getMyDriverDeliveries();
    });
    await Future.wait([_activeFuture, _myDeliveriesFuture]);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Driver", style: TextStyle(fontWeight: FontWeight.w900)),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.person_rounded),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const DriverProfileScreen()),
              );
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _refresh,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          children: [
            const Text("Active deliveries",
                style: TextStyle(fontWeight: FontWeight.w900, fontSize: 14)),
            const SizedBox(height: 10),

            FutureBuilder<List<dynamic>>(
              future: _activeFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) {
                  return _loadingBox(height: 160);
                }
                if (snap.hasError) {
                  return _errorCard("Failed to load active deliveries", snap.error.toString());
                }

                final list = snap.data ?? [];
                if (list.isEmpty) {
                  return _emptyState("No active deliveries", "When you are assigned one, it’ll appear here.");
                }

                return Column(
                  children: list.map((raw) {
                    final d = (raw as Map).cast<String, dynamic>();
                    final id = (d["id"] as num).toInt();
                    final status = (d["status"] ?? "-").toString();
                    final orderId = d["orderId"];
                    final restaurantAddress = (d["restaurantAddress"] ?? "").toString();
                    final deliveryAddress = (d["deliveryAddress"] ?? "").toString();
                    final notes = (d["deliveryNotes"] ?? "").toString();

                    return Container(
                      margin: const EdgeInsets.only(bottom: 12),
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(18),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.06),
                            blurRadius: 14,
                            offset: const Offset(0, 8),
                          ),
                        ],
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Container(
                                width: 46,
                                height: 46,
                                decoration: BoxDecoration(
                                  color: AppTheme.primaryOrange.withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(14),
                                ),
                                child: const Icon(Icons.delivery_dining_rounded,
                                    color: AppTheme.primaryOrange),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text("Delivery #$id",
                                        style: const TextStyle(
                                            fontWeight: FontWeight.w900, fontSize: 16)),
                                    const SizedBox(height: 4),
                                    Text("Order: ${orderId ?? '-'}",
                                        style: TextStyle(
                                            color: Colors.grey[700],
                                            fontWeight: FontWeight.w700)),
                                  ],
                                ),
                              ),
                              _pill(text: status, color: _statusColor(status)),
                            ],
                          ),
                          const SizedBox(height: 10),
                          if (restaurantAddress.trim().isNotEmpty)
                            _infoRow(Icons.store_mall_directory_rounded, restaurantAddress),
                          if (deliveryAddress.trim().isNotEmpty)
                            _infoRow(Icons.location_on_rounded, deliveryAddress),
                          if (notes.trim().isNotEmpty)
                            _infoRow(Icons.sticky_note_2_rounded, notes),
                          const SizedBox(height: 12),

                          Row(
                            children: [
                              Expanded(
                                child: SizedBox(
                                  height: 44,
                                  child: ElevatedButton.icon(
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: AppTheme.primaryOrange,
                                      foregroundColor: Colors.white,
                                      shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.circular(14),
                                      ),
                                    ),
                                    onPressed: () async {
                                      try {
                                        await DriverService.pickupConfirm(id);
                                        await _refresh();
                                      } catch (e) {
                                        if (!context.mounted) return;
                                        _snack(context, e.toString(), isError: true);
                                      }
                                    },
                                    icon: const Icon(Icons.inventory_2_rounded, size: 18),
                                    label: const Text("Pickup",
                                        style: TextStyle(fontWeight: FontWeight.w900)),
                                  ),
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: SizedBox(
                                  height: 44,
                                  child: OutlinedButton.icon(
                                    style: OutlinedButton.styleFrom(
                                      foregroundColor: AppTheme.primaryOrange,
                                      side: const BorderSide(color: AppTheme.primaryOrange),
                                      shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.circular(14),
                                      ),
                                    ),
                                    onPressed: () async {
                                      try {
                                        await DriverService.deliveryConfirm(id);
                                        await _refresh();
                                      } catch (e) {
                                        if (!context.mounted) return;
                                        _snack(context, e.toString(), isError: true);
                                      }
                                    },
                                    icon: const Icon(Icons.check_circle_outline, size: 18),
                                    label: const Text("Delivered",
                                        style: TextStyle(fontWeight: FontWeight.w900)),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    );
                  }).toList(),
                );
              },
            ),

            const SizedBox(height: 16),

            const Text("My deliveries",
                style: TextStyle(fontWeight: FontWeight.w900, fontSize: 14)),
            const SizedBox(height: 10),

            FutureBuilder<List<dynamic>>(
              future: _myDeliveriesFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) return _loadingBox(height: 200);
                if (snap.hasError) return _errorCard("Failed to load deliveries", snap.error.toString());

                final list = snap.data ?? [];
                if (list.isEmpty) {
                  return _emptyState("No deliveries yet", "Your delivery history will show here.");
                }

                return Column(
                  children: list.take(12).map((raw) {
                    final d = (raw as Map).cast<String, dynamic>();
                    final id = d["id"];
                    final status = (d["status"] ?? "-").toString();
                    final orderId = d["orderId"];
                    final deliveryAddress = (d["deliveryAddress"] ?? "").toString();

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
                          ),
                        ],
                      ),
                      child: Row(
                        children: [
                          Container(
                            width: 46,
                            height: 46,
                            decoration: BoxDecoration(
                              color: AppTheme.primaryOrange.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(14),
                            ),
                            child: const Icon(Icons.local_shipping_rounded,
                                color: AppTheme.primaryOrange),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text("Delivery #$id",
                                    style: const TextStyle(fontWeight: FontWeight.w900)),
                                const SizedBox(height: 4),
                                Text("Order: ${orderId ?? '-'}",
                                    style: TextStyle(
                                      color: Colors.grey[700],
                                      fontWeight: FontWeight.w700,
                                    )),
                                if (deliveryAddress.trim().isNotEmpty) ...[
                                  const SizedBox(height: 4),
                                  Text(deliveryAddress,
                                      style: TextStyle(color: Colors.grey[700])),
                                ],
                              ],
                            ),
                          ),
                          _pill(text: status, color: _statusColor(status)),
                        ],
                      ),
                    );
                  }).toList(),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  static Widget _infoRow(IconData icon, String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        children: [
          Icon(icon, size: 16, color: Colors.grey[700]),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              text,
              style: TextStyle(color: Colors.grey[800], fontWeight: FontWeight.w700),
            ),
          ),
        ],
      ),
    );
  }

  static Widget _pill({required String text, required Color color}) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(color: color.withOpacity(0.12), borderRadius: BorderRadius.circular(14)),
      child: Text(text, style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: 12)),
    );
  }

  static Color _statusColor(String s) {
    switch (s) {
      case "DELIVERED":
      case "COMPLETED":
        return const Color(0xFF2E7D32);
      case "CANCELLED":
        return const Color(0xFFC62828);
      case "ASSIGNED":
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

  static Widget _emptyState(String title, String subtitle) => Container(
    padding: const EdgeInsets.all(18),
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: Column(
      children: [
        Container(
          width: 56,
          height: 56,
          decoration: BoxDecoration(
            color: AppTheme.primaryOrange.withOpacity(0.12),
            borderRadius: BorderRadius.circular(16),
          ),
          child: const Icon(Icons.inbox_rounded, color: AppTheme.primaryOrange),
        ),
        const SizedBox(height: 10),
        Text(title, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
        const SizedBox(height: 6),
        Text(
          subtitle,
          textAlign: TextAlign.center,
          style: TextStyle(color: Colors.grey[700], fontWeight: FontWeight.w700),
        ),
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
