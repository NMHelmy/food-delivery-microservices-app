import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/admin_service.dart';

class AdminUserDetailsScreen extends StatefulWidget {
  final int userId;
  const AdminUserDetailsScreen({super.key, required this.userId});

  @override
  State<AdminUserDetailsScreen> createState() => _AdminUserDetailsScreenState();
}

class _AdminUserDetailsScreenState extends State<AdminUserDetailsScreen> {
  late Future<Map<String, dynamic>> _userFuture;
  late Future<List<dynamic>> _addressesFuture;

  @override
  void initState() {
    super.initState();
    _userFuture = AdminService.getUser(widget.userId);
    _addressesFuture = AdminService.getUserAddresses(widget.userId);
  }

  Future<void> _refresh() async {
    setState(() {
      _userFuture = AdminService.getUser(widget.userId);
      _addressesFuture = AdminService.getUserAddresses(widget.userId);
    });
    await Future.wait([_userFuture, _addressesFuture]);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("User #${widget.userId}", style: const TextStyle(fontWeight: FontWeight.w900)),
      ),
      body: RefreshIndicator(
        onRefresh: _refresh,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            FutureBuilder<Map<String, dynamic>>(
              future: _userFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) return _loadingBox(120);
                if (snap.hasError) return _errorCard("Failed to load user", snap.error.toString());

                final u = snap.data!;
                return _infoCard(
                  title: u["fullName"]?.toString() ?? "User",
                  subtitle: u["email"]?.toString() ?? "",
                  rows: {
                    "User ID": (u["userId"] ?? u["id"] ?? widget.userId).toString(),
                    "Phone": (u["phoneNumber"] ?? u["phone"] ?? "-").toString(),
                    "Role": (u["role"] ?? "-").toString(),
                    "Active": (u["isActive"] ?? "-").toString(),
                    "Email verified": (u["isEmailVerified"] ?? "-").toString(),
                  },
                );
              },
            ),
            const SizedBox(height: 14),
            const Text("Addresses", style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
            const SizedBox(height: 10),
            FutureBuilder<List<dynamic>>(
              future: _addressesFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) return _loadingBox(160);
                if (snap.hasError) return _errorCard("Failed to load addresses", snap.error.toString());

                final list = snap.data ?? [];
                if (list.isEmpty) return _emptyState("No addresses", "This user has no saved addresses.");

                return Column(
                  children: list.map((raw) {
                    final a = (raw as Map).cast<String, dynamic>();
                    final id = a["id"];

                    // Get all possible address fields
                    final addressLine = (a["addressLine"] ?? a["address"] ?? "").toString();
                    final street = (a["street"] ?? "").toString();
                    final building = (a["building"] ?? a["buildingNumber"] ?? "").toString();
                    final floor = (a["floor"] ?? "").toString();
                    final apartment = (a["apartment"] ?? a["apartmentNumber"] ?? "").toString();
                    final city = (a["city"] ?? "").toString();
                    final district = (a["district"] ?? a["area"] ?? "").toString();
                    final postalCode = (a["postalCode"] ?? a["zipCode"] ?? "").toString();
                    final country = (a["country"] ?? "").toString();
                    final label = (a["label"] ?? "").toString();
                    final isDefault = (a["isDefault"] ?? false) == true;

                    // Build comprehensive address display
                    final List<String> addressParts = [];

                    if (addressLine.isNotEmpty && addressLine != "-") {
                      addressParts.add(addressLine);
                    } else {
                      // Build from individual parts
                      if (building.isNotEmpty) addressParts.add("Building $building");
                      if (floor.isNotEmpty) addressParts.add("Floor $floor");
                      if (apartment.isNotEmpty) addressParts.add("Apt $apartment");
                      if (street.isNotEmpty) addressParts.add(street);
                    }

                    if (district.isNotEmpty) addressParts.add(district);
                    if (city.isNotEmpty) addressParts.add(city);
                    if (postalCode.isNotEmpty) addressParts.add(postalCode);
                    if (country.isNotEmpty) addressParts.add(country);

                    final fullAddress = addressParts.isEmpty ? "No address details" : addressParts.join(", ");

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
                              Container(
                                width: 46,
                                height: 46,
                                decoration: BoxDecoration(
                                  color: AppTheme.primaryOrange.withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(14),
                                ),
                                child: const Icon(
                                  Icons.location_on_rounded,
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
                                        Text(
                                          label.isNotEmpty ? label : "Address #$id",
                                          style: const TextStyle(fontWeight: FontWeight.w900),
                                        ),
                                        if (isDefault) ...[
                                          const SizedBox(width: 8),
                                          _pill(text: "DEFAULT", color: const Color(0xFF2E7D32)),
                                        ],
                                      ],
                                    ),
                                    const SizedBox(height: 6),
                                    Text(
                                      fullAddress,
                                      style: TextStyle(
                                        color: Colors.grey[800],
                                        fontWeight: FontWeight.w600,
                                        height: 1.4,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),

                          // Show detailed breakdown if available
                          if (building.isNotEmpty || floor.isNotEmpty || apartment.isNotEmpty) ...[
                            const SizedBox(height: 12),
                            const Divider(height: 1),
                            const SizedBox(height: 12),
                            Wrap(
                              spacing: 8,
                              runSpacing: 8,
                              children: [
                                if (building.isNotEmpty)
                                  _detailChip(icon: Icons.apartment, label: "Bldg $building"),
                                if (floor.isNotEmpty)
                                  _detailChip(icon: Icons.layers, label: "Floor $floor"),
                                if (apartment.isNotEmpty)
                                  _detailChip(icon: Icons.door_front_door, label: "Apt $apartment"),
                              ],
                            ),
                          ],

                          // Address ID for reference
                          const SizedBox(height: 8),
                          Text(
                            "ID: $id",
                            style: TextStyle(
                              color: Colors.grey[500],
                              fontSize: 11,
                              fontWeight: FontWeight.w700,
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

  static Widget _detailChip({required IconData icon, required String label}) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: Colors.grey[700]),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              color: Colors.grey[700],
              fontSize: 12,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }

  static Widget _pill({required String text, required Color color}) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
    decoration: BoxDecoration(
      color: color.withOpacity(0.12),
      borderRadius: BorderRadius.circular(12),
    ),
    child: Text(
      text,
      style: TextStyle(
        color: color,
        fontWeight: FontWeight.w900,
        fontSize: 11,
      ),
    ),
  );

  static Widget _loadingBox(double h) => Container(
    height: h,
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

  static Widget _infoCard({
    required String title,
    required String subtitle,
    required Map<String, String> rows,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18)),
          if (subtitle.trim().isNotEmpty) ...[
            const SizedBox(height: 4),
            Text(subtitle, style: TextStyle(color: Colors.grey[700], fontWeight: FontWeight.w700)),
          ],
          const SizedBox(height: 12),
          ...rows.entries.map((e) => Padding(
            padding: const EdgeInsets.only(bottom: 6),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    e.key,
                    style: TextStyle(color: Colors.grey[700], fontWeight: FontWeight.w700),
                  ),
                ),
                Text(e.value, style: const TextStyle(fontWeight: FontWeight.w900)),
              ],
            ),
          )),
        ],
      ),
    );
  }
}