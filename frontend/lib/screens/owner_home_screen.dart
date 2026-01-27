import 'package:flutter/material.dart';

import '../theme/app_theme.dart';
import '../services/owner_service.dart';
import 'owner_restaurant_form_screen.dart';
import 'owner_menu_screen.dart';
import 'owner_deliveries_management_screen.dart';

class OwnerHomeScreen extends StatefulWidget {
  const OwnerHomeScreen({super.key});

  @override
  State<OwnerHomeScreen> createState() => OwnerHomeScreenState();
}

class OwnerHomeScreenState extends State<OwnerHomeScreen> {
  late Future<List<dynamic>> restaurantsFuture;
  late Future<List<dynamic>> deliveriesFuture;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  void _loadData() {
    restaurantsFuture = OwnerService.getMyRestaurants();
    deliveriesFuture = OwnerService.getMyRestaurantDeliveries();
  }

  Future<void> refresh() async {
    setState(() {
      _loadData();
    });
    await Future.wait([restaurantsFuture, deliveriesFuture]);
  }

  Future<void> _createRestaurant() async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => const OwnerRestaurantFormScreen(),
      ),
    );

    // Refresh if restaurant was created successfully
    if (result == true && mounted) {
      await refresh();
      snack(context, "Restaurant created successfully");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Owner", style: TextStyle(fontWeight: FontWeight.w900)),
        centerTitle: true,
        actions: [
          // Deliveries button at top
          IconButton(
            tooltip: 'All deliveries',
            icon: const Icon(Icons.local_shipping_rounded),
            onPressed: () async {
              await Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const OwnerDeliveriesManagementScreen()),
              );
              await refresh();
            },
          ),
          // Add restaurant button at top
          IconButton(
            tooltip: 'Create restaurant',
            icon: const Icon(Icons.add_rounded),
            onPressed: _createRestaurant,
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: refresh,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          children: [
            const Text("My Restaurants",
                style: TextStyle(fontWeight: FontWeight.w900, fontSize: 14)),
            const SizedBox(height: 10),
            FutureBuilder<List<dynamic>>(
              future: restaurantsFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) {
                  return loadingBox(height: 160);
                }
                if (snap.hasError) {
                  return errorCard("Failed to load restaurants", snap.error.toString());
                }

                final list = snap.data ?? [];
                if (list.isEmpty) {
                  return emptyState(
                    "No restaurants",
                    "Create a restaurant to start managing orders and deliveries.",
                  );
                }

                return Column(
                  children: list.map((raw) {
                    final r = (raw as Map).cast<String, dynamic>();
                    final id = (r['id'] as num).toInt();
                    final name = (r['name'] ?? 'Restaurant').toString();
                    final cuisine = (r['cuisine'] ?? '').toString();
                    final address = (r['address'] ?? '').toString();
                    final phone = (r['phone'] ?? '').toString();
                    final isActive = (r['isActive'] ?? false) == true;
                    final imageUrl = (r['imageUrl'] ?? '').toString();

                    return Container(
                      margin: const EdgeInsets.only(bottom: 12),
                      decoration: BoxDecoration(
                        // Gray out inactive restaurants
                        color: isActive ? Colors.white : Colors.grey[100],
                        borderRadius: BorderRadius.circular(18),
                        // Add border for inactive restaurants
                        border: isActive ? null : Border.all(
                          color: Colors.grey[400]!,
                          width: 2,
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(isActive ? 0.06 : 0.03),
                            blurRadius: 14,
                            offset: const Offset(0, 8),
                          )
                        ],
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          ClipRRect(
                            borderRadius:
                            const BorderRadius.vertical(top: Radius.circular(18)),
                            child: SizedBox(
                              height: 140,
                              width: double.infinity,
                              child: Stack(
                                children: [
                                  // Restaurant image
                                  imageUrl.isNotEmpty
                                      ? Image.network(
                                    imageUrl,
                                    fit: BoxFit.cover,
                                    width: double.infinity,
                                    height: 140,
                                    errorBuilder: (_, __, ___) => imagePlaceholder(),
                                    // Grayscale filter for inactive restaurants
                                    color: isActive ? null : Colors.grey,
                                    colorBlendMode: isActive ? null : BlendMode.saturation,
                                  )
                                      : imagePlaceholder(),

                                  // Overlay for inactive restaurants
                                  if (!isActive)
                                    Container(
                                      width: double.infinity,
                                      height: 140,
                                      color: Colors.black.withOpacity(0.3),
                                      child: Center(
                                        child: Container(
                                          padding: const EdgeInsets.symmetric(
                                              horizontal: 16, vertical: 8),
                                          decoration: BoxDecoration(
                                            color: Colors.red,
                                            borderRadius: BorderRadius.circular(8),
                                          ),
                                          child: const Text(
                                            "Restaurant Not Active",
                                            style: TextStyle(
                                              color: Colors.white,
                                              fontWeight: FontWeight.w900,
                                              fontSize: 14,
                                            ),
                                          ),
                                        ),
                                      ),
                                    ),
                                ],
                              ),
                            ),
                          ),
                          Padding(
                            padding: const EdgeInsets.all(14),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Expanded(
                                      child: Text(
                                        name,
                                        style: const TextStyle(
                                            fontWeight: FontWeight.w900, fontSize: 16),
                                      ),
                                    ),
                                    pill(
                                      text: isActive ? "ACTIVE" : "INACTIVE",
                                      color: isActive
                                          ? const Color(0xFF2E7D32)
                                          : const Color(0xFFC62828),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 6),
                                if (cuisine.trim().isNotEmpty)
                                  Text(
                                    cuisine,
                                    style: TextStyle(
                                        color: Colors.grey[700],
                                        fontWeight: FontWeight.w700),
                                  ),
                                if (address.trim().isNotEmpty) ...[
                                  const SizedBox(height: 4),
                                  Text(address, style: TextStyle(color: Colors.grey[700])),
                                ],
                                if (phone.trim().isNotEmpty) ...[
                                  const SizedBox(height: 4),
                                  Text(phone, style: TextStyle(color: Colors.grey[700])),
                                ],
                                const SizedBox(height: 12),

                                // ONLY 2 BUTTONS: Manage Menu and Edit Restaurant

                                // Show message if restaurant is inactive
                                if (!isActive) ...[
                                  Container(
                                    padding: const EdgeInsets.all(12),
                                    decoration: BoxDecoration(
                                      color: Colors.orange[50],
                                      borderRadius: BorderRadius.circular(12),
                                      border: Border.all(color: Colors.orange[200]!),
                                    ),
                                    child: Row(
                                      children: [
                                        Icon(Icons.info_outline,
                                            color: Colors.orange[800], size: 20),
                                        const SizedBox(width: 8),
                                        Expanded(
                                          child: Text(
                                            "This restaurant is not active. Activate it to receive orders.",
                                            style: TextStyle(
                                              color: Colors.orange[900],
                                              fontSize: 12,
                                              fontWeight: FontWeight.w700,
                                            ),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(height: 12),
                                ],

                                // Manage Menu button
                                SizedBox(
                                  height: 44,
                                  width: double.infinity,
                                  child: ElevatedButton.icon(
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: isActive
                                          ? AppTheme.primaryOrange
                                          : Colors.grey[400],
                                      foregroundColor: Colors.white,
                                      shape: RoundedRectangleBorder(
                                          borderRadius: BorderRadius.circular(14)),
                                    ),
                                    onPressed: isActive ? () async {
                                      await Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder: (_) => OwnerMenuScreen(
                                            restaurantId: id,
                                            restaurantName: name,
                                          ),
                                        ),
                                      );
                                      await refresh();
                                    } : null,
                                    icon: const Icon(Icons.restaurant_menu_rounded, size: 18),
                                    label: Text(
                                      isActive ? "Manage menu" : "Menu (inactive)",
                                      style: const TextStyle(fontWeight: FontWeight.w900),
                                    ),
                                  ),
                                ),

                                const SizedBox(height: 10),

                                // Edit Restaurant button (always enabled)
                                SizedBox(
                                  height: 44,
                                  width: double.infinity,
                                  child: OutlinedButton.icon(
                                    style: OutlinedButton.styleFrom(
                                      foregroundColor: AppTheme.primaryOrange,
                                      side: const BorderSide(color: AppTheme.primaryOrange),
                                      shape: RoundedRectangleBorder(
                                          borderRadius: BorderRadius.circular(14)),
                                    ),
                                    onPressed: () async {
                                      final result = await Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder: (_) =>
                                              OwnerRestaurantFormScreen(restaurant: r),
                                        ),
                                      );
                                      if (result == true && mounted) {
                                        await refresh();
                                        snack(context, "Restaurant updated successfully");
                                      }
                                    },
                                    icon: const Icon(Icons.edit_rounded, size: 18),
                                    label: const Text("Edit restaurant",
                                        style: TextStyle(fontWeight: FontWeight.w900)),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    );
                  }).toList(),
                );
              },
            ),
            const SizedBox(height: 16),

            // Deliveries section at bottom
            const Text("My Restaurant Deliveries",
                style: TextStyle(fontWeight: FontWeight.w900, fontSize: 14)),
            const SizedBox(height: 10),
            FutureBuilder<List<dynamic>>(
              future: deliveriesFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) {
                  return loadingBox(height: 180);
                }
                if (snap.hasError) {
                  return errorCard("Failed to load deliveries", snap.error.toString());
                }
                final list = snap.data ?? [];
                if (list.isEmpty) {
                  return emptyState(
                    "No deliveries yet...",
                    "When deliveries are created for your restaurants, they'll show here.",
                  );
                }

                return Column(
                  children: list.take(12).map((raw) {
                    final d = (raw as Map).cast<String, dynamic>();
                    final id = d['id'];
                    final status = (d['status'] ?? '-').toString();
                    final orderId = d['orderId'];
                    final driverId = d['driverId'];
                    final deliveryAddress = (d['deliveryAddress'] ?? '').toString();

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
                                Text("Delivery $id",
                                    style: const TextStyle(fontWeight: FontWeight.w900)),
                                const SizedBox(height: 4),
                                Text(
                                  "Order ${orderId ?? '-'} • Driver ${driverId ?? 'Unassigned'}",
                                  style: TextStyle(
                                      color: Colors.grey[700], fontWeight: FontWeight.w700),
                                ),
                                if (deliveryAddress.trim().isNotEmpty) ...[
                                  const SizedBox(height: 4),
                                  Text(deliveryAddress, style: TextStyle(color: Colors.grey[700])),
                                ],
                              ],
                            ),
                          ),
                          pill(text: status, color: statusColor(status)),
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

  static Widget imagePlaceholder() => Container(
    color: AppTheme.primaryOrange.withOpacity(0.12),
    child: const Center(
      child: Icon(Icons.restaurant, color: AppTheme.primaryOrange, size: 40),
    ),
  );

  static Widget pill({required String text, required Color color}) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
    decoration: BoxDecoration(
      color: color.withOpacity(0.12),
      borderRadius: BorderRadius.circular(14),
    ),
    child: Text(text,
        style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: 12)),
  );

  static Color statusColor(String s) {
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

  static Widget loadingBox({required double height}) => Container(
    height: height,
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: const Center(child: CircularProgressIndicator()),
  );

  static Widget errorCard(String title, String message) => Container(
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

  static Widget emptyState(String title, String subtitle) => Container(
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
        Text(title,
            style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
        const SizedBox(height: 6),
        Text(subtitle,
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.grey[700], fontWeight: FontWeight.w700)),
      ],
    ),
  );

  static void snack(BuildContext context, String text, {bool isError = false}) {
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