import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/admin_service.dart';

class AdminDeliveriesScreen extends StatefulWidget {
  const AdminDeliveriesScreen({super.key});

  @override
  State<AdminDeliveriesScreen> createState() => _AdminDeliveriesScreenState();
}

class _AdminDeliveriesScreenState extends State<AdminDeliveriesScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  List<dynamic> _deliveries = [];
  List<dynamic> _drivers = [];
  bool _loadingDeliveries = true;
  bool _loadingDrivers = true;
  bool _loadingAction = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    await Future.wait([
      _loadDeliveries(),
      _loadDrivers(),
    ]);
  }

  Future<void> _loadDeliveries() async {
    setState(() => _loadingDeliveries = true);
    try {
      // Try to get all deliveries - this endpoint might not exist
      final deliveries = await AdminService.getAllDeliveries();
      print("DEBUG: Loaded ${deliveries.length} deliveries");
      setState(() {
        _deliveries = deliveries;
        _loadingDeliveries = false;
      });
    } catch (e) {
      print("DEBUG: Error loading deliveries: $e");
      // If endpoint doesn't exist, show empty state
      setState(() {
        _deliveries = [];
        _loadingDeliveries = false;
      });
      _snack("Deliveries endpoint not available. Add /deliveries/admin/all to backend.", isError: true);
    }
  }

  Future<void> _loadDrivers() async {
    setState(() => _loadingDrivers = true);
    try {
      // Get ALL drivers, not just available
      final drivers = await AdminService.getAllDrivers();
      print("DEBUG: Loaded ${drivers.length} drivers");
      setState(() {
        _drivers = drivers;
        _loadingDrivers = false;
      });
    } catch (e) {
      print("DEBUG: Error loading drivers: $e");
      setState(() {
        _drivers = [];
        _loadingDrivers = false;
      });
      _snack("Error loading drivers: $e", isError: true);
    }
  }

  Future<void> _showAssignDriverDialog(int deliveryId, int? currentDriverId) async {
    if (_drivers.isEmpty) {
      _snack("No drivers available", isError: true);
      return;
    }

    final selectedDriver = await showDialog<int>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Assign Driver"),
        content: SizedBox(
          width: double.maxFinite,
          child: ListView.builder(
            shrinkWrap: true,
            itemCount: _drivers.length,
            itemBuilder: (context, i) {
              final d = (_drivers[i] as Map).cast<String, dynamic>();
              final id = (d["userId"] ?? d["id"] as num).toInt();
              final name = (d["fullName"] ?? "Driver").toString();
              final phone = (d["phoneNumber"] ?? "").toString();
              final status = (d["driverStatus"] ?? "UNKNOWN").toString();
              final isCurrent = id == currentDriverId;

              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: isCurrent
                      ? AppTheme.primaryOrange.withOpacity(0.2)
                      : AppTheme.primaryOrange.withOpacity(0.12),
                  child: Icon(
                    isCurrent ? Icons.check : Icons.delivery_dining,
                    color: AppTheme.primaryOrange,
                  ),
                ),
                title: Text(
                  name,
                  style: TextStyle(
                    fontWeight: isCurrent ? FontWeight.w900 : FontWeight.w700,
                  ),
                ),
                subtitle: Text("$phone • $status"),
                trailing: isCurrent ? const Text("Current", style: TextStyle(color: AppTheme.primaryOrange)) : null,
                onTap: () => Navigator.pop(context, id),
              );
            },
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("Cancel"),
          ),
        ],
      ),
    );

    if (selectedDriver == null) return;

    setState(() => _loadingAction = true);
    try {
      await AdminService.assignDriver(deliveryId, selectedDriver);
      _snack("Driver assigned to delivery #$deliveryId");
      await _loadDeliveries();
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
        title: const Text("Delivery Management", style: TextStyle(fontWeight: FontWeight.w900)),
        bottom: TabBar(
          controller: _tabController,
          tabs: [
            Tab(
              icon: const Icon(Icons.local_shipping),
              text: "Deliveries (${_deliveries.length})",
            ),
            Tab(
              icon: const Icon(Icons.delivery_dining),
              text: "Drivers (${_drivers.length})",
            ),
          ],
        ),
        actions: [
          IconButton(
            onPressed: _loadingAction ? null : _loadData,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildDeliveriesTab(),
          _buildDriversTab(),
        ],
      ),
    );
  }

  Widget _buildDeliveriesTab() {
    if (_loadingDeliveries) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_deliveries.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.local_shipping_outlined, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              "No deliveries found",
              style: TextStyle(fontWeight: FontWeight.w900, color: Colors.grey[700]),
            ),
            const SizedBox(height: 8),
            Text(
              "Deliveries will appear here once created",
              style: TextStyle(color: Colors.grey[600]),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: _loadDeliveries,
              icon: const Icon(Icons.refresh),
              label: const Text("Retry"),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadDeliveries,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _deliveries.length,
        itemBuilder: (context, i) {
          final d = (_deliveries[i] as Map).cast<String, dynamic>();
          final id = (d["id"] ?? d["deliveryId"] as num?)?.toInt() ?? i;
          final orderId = d["orderId"];
          final customerId = d["customerId"];
          final restaurantId = d["restaurantId"];
          final driverId = d["driverId"];
          final status = (d["status"] ?? "PENDING").toString();
          final createdAt = d["createdAt"]?.toString() ?? "";
          final deliveryNotes = d["deliveryNotes"]?.toString();

          // Get driver name if assigned
          String? driverName;
          if (driverId != null) {
            final driver = _drivers.where((dr) {
              final drId = ((dr as Map)["userId"] ?? dr["id"] as num).toInt();
              return drId == driverId;
            }).firstOrNull;
            if (driver != null) {
              driverName = ((driver as Map)["fullName"] ?? "Driver #$driverId").toString();
            }
          }

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
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header with ID and Status
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: _deliveryStatusColor(status).withOpacity(0.1),
                    borderRadius: const BorderRadius.only(
                      topLeft: Radius.circular(16),
                      topRight: Radius.circular(16),
                    ),
                  ),
                  child: Row(
                    children: [
                      Container(
                        width: 46,
                        height: 46,
                        decoration: BoxDecoration(
                          color: _deliveryStatusColor(status).withOpacity(0.2),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Icon(
                          Icons.local_shipping,
                          color: _deliveryStatusColor(status),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "Delivery #$id",
                              style: const TextStyle(
                                fontWeight: FontWeight.w900,
                                fontSize: 16,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              "Order #$orderId",
                              style: TextStyle(
                                color: Colors.grey[700],
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ],
                        ),
                      ),
                      _pill(text: status, color: _deliveryStatusColor(status)),
                    ],
                  ),
                ),

                // Details
                Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _infoRow(Icons.person, "Customer", "#$customerId"),
                      const SizedBox(height: 8),
                      _infoRow(Icons.store, "Restaurant", "#$restaurantId"),
                      const SizedBox(height: 8),
                      if (driverId != null && driverName != null)
                        _infoRow(Icons.delivery_dining, "Driver", "$driverName (#$driverId)")
                      else
                        _infoRow(Icons.delivery_dining, "Driver", "Not assigned", isError: true),
                      if (deliveryNotes != null && deliveryNotes.isNotEmpty) ...[
                        const SizedBox(height: 12),
                        const Divider(height: 1),
                        const SizedBox(height: 12),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Icon(Icons.note, size: 16, color: Colors.grey[600]),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                deliveryNotes,
                                style: TextStyle(
                                  color: Colors.grey[700],
                                  fontStyle: FontStyle.italic,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ],
                  ),
                ),

                // Actions
                const Divider(height: 1),
                Padding(
                  padding: const EdgeInsets.all(10),
                  child: Row(
                    children: [
                      if (createdAt.isNotEmpty)
                        Expanded(
                          child: Text(
                            createdAt.split('T')[0],
                            style: TextStyle(
                              color: Colors.grey[600],
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      const Spacer(),
                      if (driverId == null)
                        ElevatedButton.icon(
                          onPressed: _loadingAction ? null : () => _showAssignDriverDialog(id, driverId),
                          icon: const Icon(Icons.person_add, size: 18),
                          label: const Text("Assign Driver"),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppTheme.primaryOrange,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                          ),
                        )
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _infoRow(IconData icon, String label, String value, {bool isError = false}) {
    return Row(
      children: [
        Icon(icon, size: 16, color: isError ? Colors.red : Colors.grey[600]),
        const SizedBox(width: 8),
        Text(
          "$label: ",
          style: TextStyle(
            color: Colors.grey[700],
            fontWeight: FontWeight.w700,
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(
              fontWeight: FontWeight.w900,
              color: isError ? Colors.red : Colors.black,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildDriversTab() {
    if (_loadingDrivers) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_drivers.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.person_outline, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              "No drivers found",
              style: TextStyle(fontWeight: FontWeight.w900, color: Colors.grey[700]),
            ),
            const SizedBox(height: 8),
            Text(
              "Register drivers to see them here",
              style: TextStyle(color: Colors.grey[600]),
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: _loadDrivers,
              icon: const Icon(Icons.refresh),
              label: const Text("Retry"),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadDrivers,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _drivers.length,
        itemBuilder: (context, i) {
          final d = (_drivers[i] as Map).cast<String, dynamic>();
          final id = (d["userId"] ?? d["id"] as num).toInt();
          final name = (d["fullName"] ?? "Driver").toString();
          final phone = (d["phoneNumber"] ?? "").toString();
          final email = (d["email"] ?? "").toString();
          final vehicle = (d["vehicleType"] ?? "").toString();
          final vehicleNumber = (d["vehicleNumber"] ?? "").toString();
          final status = (d["driverStatus"] ?? "UNKNOWN").toString();
          final isActive = (d["active"] ?? false) == true;

          return Container(
            margin: const EdgeInsets.only(bottom: 12),
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: isActive ? Colors.transparent : Colors.red.withOpacity(0.3),
                width: 2,
              ),
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
                    Container(
                      width: 50,
                      height: 50,
                      decoration: BoxDecoration(
                        color: AppTheme.primaryOrange.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: const Icon(
                        Icons.delivery_dining,
                        color: AppTheme.primaryOrange,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Expanded(
                                child: Text(
                                  name,
                                  style: const TextStyle(fontWeight: FontWeight.w900),
                                ),
                              ),
                              _pill(text: status, color: _driverStatusColor(status)),
                            ],
                          ),
                          const SizedBox(height: 4),
                          Text(
                            "ID: $id",
                            style: TextStyle(
                              color: Colors.grey[600],
                              fontWeight: FontWeight.w700,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                const Divider(height: 1),
                const SizedBox(height: 12),
                if (phone.isNotEmpty)
                  _driverInfoRow(Icons.phone, phone),
                if (email.isNotEmpty) ...[
                  const SizedBox(height: 6),
                  _driverInfoRow(Icons.email, email),
                ],
                if (vehicle.isNotEmpty || vehicleNumber.isNotEmpty) ...[
                  const SizedBox(height: 6),
                  _driverInfoRow(
                    Icons.directions_car,
                    [vehicle, vehicleNumber].where((s) => s.isNotEmpty).join(" • "),
                  ),
                ],
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: ElevatedButton.icon(
                        onPressed: _loadingAction
                            ? null
                            : () async {
                          setState(() => _loadingAction = true);
                          try {
                            if (isActive) {
                              await AdminService.deactivateDriver(id);
                              _snack('Driver deactivated');
                            } else {
                              await AdminService.activateDriver(id);
                              _snack('Driver activated');
                            }
                            await _loadDrivers();
                          } catch (e) {
                            _snack('Failed: $e', isError: true);
                          } finally {
                            if (mounted) setState(() => _loadingAction = false);
                          }
                        },
                        icon: Icon(isActive ? Icons.block : Icons.check_circle_outline, size: 18),
                        label: Text(isActive ? 'Deactivate' : 'Activate'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: isActive ? Colors.red : AppTheme.primaryOrange,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(vertical: 10),
                        ),
                      ),
                    ),
                  ],
                ),

                if (!isActive) ...[
                  const SizedBox(height: 12),
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: Colors.red.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Row(
                      children: [
                        Icon(Icons.warning, size: 16, color: Colors.red),
                        SizedBox(width: 8),
                        Text(
                          "Account Inactive",
                          style: TextStyle(
                            color: Colors.red,
                            fontWeight: FontWeight.w700,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _driverInfoRow(IconData icon, String text) {
    return Row(
      children: [
        Icon(icon, size: 14, color: Colors.grey[600]),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: TextStyle(
              color: Colors.grey[700],
              fontWeight: FontWeight.w600,
              fontSize: 13,
            ),
          ),
        ),
      ],
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
      style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: 12),
    ),
  );

  static Color _deliveryStatusColor(String s) {
    switch (s.toUpperCase()) {
      case "DELIVERED":
        return const Color(0xFF2E7D32);
      case "CANCELLED":
        return const Color(0xFFC62828);
      case "IN_TRANSIT":
      case "PICKED_UP":
        return const Color(0xFFF57C00);
      default:
        return const Color(0xFF1565C0);
    }
  }

  static Color _driverStatusColor(String s) {
    switch (s.toUpperCase()) {
      case "AVAILABLE":
        return const Color(0xFF2E7D32);
      case "BUSY":
        return const Color(0xFFF57C00);
      case "OFFLINE":
        return const Color(0xFF757575);
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

extension FirstOrNullExtension<E> on Iterable<E> {
  E? get firstOrNull => isEmpty ? null : first;
}