import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/owner_service.dart';

class OwnerDeliveriesManagementScreen extends StatefulWidget {
  const OwnerDeliveriesManagementScreen({super.key});

  @override
  State<OwnerDeliveriesManagementScreen> createState() => _OwnerDeliveriesManagementScreenState();
}

class _OwnerDeliveriesManagementScreenState extends State<OwnerDeliveriesManagementScreen> {
  late Future<List<dynamic>> _deliveriesFuture;

  @override
  void initState() {
    super.initState();
    _deliveriesFuture = OwnerService.getMyRestaurantDeliveries();
  }

  Future<void> _refresh() async {
    setState(() => _deliveriesFuture = OwnerService.getMyRestaurantDeliveries());
    await _deliveriesFuture;
  }

  Future<void> _assignDriver(int deliveryId) async {
    final controller = TextEditingController();
    final driverId = await showDialog<int?>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Assign driver'),
        content: TextField(
          controller: controller,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(labelText: 'Driver ID'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, null), child: const Text('Cancel')),
          TextButton(
            onPressed: () {
              final v = int.tryParse(controller.text.trim());
              Navigator.pop(context, v);
            },
            child: const Text('Assign'),
          ),
        ],
      ),
    );

    if (driverId == null) return;

    try {
      await OwnerService.assignDriver(deliveryId, driverId);
      await _refresh();
    } catch (e) {
      if (!context.mounted) return;
      _snack(context, e.toString(), isError: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Deliveries", style: TextStyle(fontWeight: FontWeight.w900)),
        centerTitle: true,
      ),
      body: RefreshIndicator(
        onRefresh: _refresh,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          children: [
            FutureBuilder<List<dynamic>>(
              future: _deliveriesFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) {
                  return _loadingBox(height: 220);
                }
                if (snap.hasError) {
                  return _errorCard("Failed to load deliveries", snap.error.toString());
                }

                final list = snap.data ?? [];
                if (list.isEmpty) return _empty("No deliveries", "They will appear here.");

                return Column(
                  children: list.map((raw) {
                    final d = (raw as Map).cast<String, dynamic>();
                    final id = (d['id'] as num).toInt();
                    final status = (d['status'] ?? '-').toString();
                    final orderId = d['orderId'];
                    final driverId = d['driverId'];
                    final restaurantAddress = (d['restaurantAddress'] ?? '').toString();
                    final deliveryAddress = (d['deliveryAddress'] ?? '').toString();
                    final notes = (d['deliveryNotes'] ?? '').toString();
                    final eta = (d['estimatedDeliveryTime'] ?? '').toString();

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
                                child: Text("Delivery $id",
                                    style: const TextStyle(
                                        fontWeight: FontWeight.w900, fontSize: 16)),
                              ),
                              _pill(text: status, color: _statusColor(status)),
                            ],
                          ),
                          const SizedBox(height: 6),
                          Text("Order: ${orderId ?? '-'} • Driver: ${driverId ?? 'Unassigned'}",
                              style: TextStyle(
                                  color: Colors.grey[700], fontWeight: FontWeight.w800)),
                          if (restaurantAddress.trim().isNotEmpty) ...[
                            const SizedBox(height: 6),
                            Text("From: $restaurantAddress", style: TextStyle(color: Colors.grey[800])),
                          ],
                          if (deliveryAddress.trim().isNotEmpty) ...[
                            const SizedBox(height: 4),
                            Text("To: $deliveryAddress", style: TextStyle(color: Colors.grey[800])),
                          ],
                          if (eta.trim().isNotEmpty) ...[
                            const SizedBox(height: 4),
                            Text("ETA: $eta", style: TextStyle(color: Colors.grey[700])),
                          ],
                          if (notes.trim().isNotEmpty) ...[
                            const SizedBox(height: 6),
                            Text("Notes: $notes", style: TextStyle(color: Colors.grey[800])),
                          ],
                          const SizedBox(height: 12),

                          DropdownButtonFormField<String>(
                            value: status,
                            decoration: InputDecoration(
                              labelText: "Update delivery status",
                              border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                            ),
                            items: const [
                              DropdownMenuItem(value: "PENDING", child: Text("PENDING")),
                              DropdownMenuItem(value: "ASSIGNED", child: Text("ASSIGNED")),
                              DropdownMenuItem(value: "PICKED_UP", child: Text("PICKED_UP")),
                              DropdownMenuItem(value: "DELIVERED", child: Text("DELIVERED")),
                              DropdownMenuItem(value: "CANCELLED", child: Text("CANCELLED")),
                            ],
                            onChanged: (newStatus) async {
                              if (newStatus == null || newStatus == status) return;
                              try {
                                await OwnerService.updateDeliveryStatus(id, newStatus);
                                await _refresh();
                              } catch (e) {
                                if (!context.mounted) return;
                                _snack(context, e.toString(), isError: true);
                              }
                            },
                          ),

                          const SizedBox(height: 10),
                          SizedBox(
                            height: 44,
                            width: double.infinity,
                            child: ElevatedButton.icon(
                              onPressed: () => _assignDriver(id),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: AppTheme.primaryOrange,
                                foregroundColor: Colors.white,
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                              ),
                              icon: const Icon(Icons.person_add_alt_1_rounded, size: 18),
                              label: const Text("Assign driver",
                                  style: TextStyle(fontWeight: FontWeight.w900)),
                            ),
                          ),
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
      case "DELIVERED":
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
