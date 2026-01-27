import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/admin_service.dart';

class AdminRestaurantsScreen extends StatefulWidget {
  const AdminRestaurantsScreen({super.key});

  @override
  State<AdminRestaurantsScreen> createState() => _AdminRestaurantsScreenState();
}

class _AdminRestaurantsScreenState extends State<AdminRestaurantsScreen> {
  Future<List<dynamic>>? _allUsersFuture;
  Future<List<dynamic>>? _restaurantsFuture;

  List<dynamic> _restaurantOwners = [];
  int? _selectedOwnerId;
  String _ownerSearchQuery = "";

  @override
  void initState() {
    super.initState();
    _loadUsers();
  }

  Future<void> _loadUsers() async {
    setState(() {
      _allUsersFuture = AdminService.getUsers();
    });

    try {
      final users = await _allUsersFuture!;
      setState(() {
        // Filter only restaurant owners
        _restaurantOwners = users.where((u) {
          final role = ((u as Map)["role"] ?? "").toString().toUpperCase();
          return role == "RESTAURANT_OWNER";
        }).toList();
      });
    } catch (e) {
      _snack("Error loading users: $e", isError: true);
    }
  }

  Future<void> _loadRestaurants(int ownerId) async {
    setState(() {
      _selectedOwnerId = ownerId;
      _restaurantsFuture = AdminService.getRestaurantsByOwner(ownerId);
    });
  }

  List<dynamic> get _filteredOwners {
    if (_ownerSearchQuery.isEmpty) return _restaurantOwners;

    return _restaurantOwners.where((owner) {
      final o = (owner as Map).cast<String, dynamic>();
      final id = (o["userId"] ?? o["id"] ?? "").toString().toLowerCase();
      final name = (o["fullName"] ?? "").toString().toLowerCase();
      final email = (o["email"] ?? "").toString().toLowerCase();

      final query = _ownerSearchQuery.toLowerCase();
      return id.contains(query) || name.contains(query) || email.contains(query);
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Restaurants by Owner", style: TextStyle(fontWeight: FontWeight.w900)),
        actions: [
          IconButton(
            onPressed: _loadUsers,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: Column(
        children: [
          // Owner selector section
          Container(
            color: Colors.grey[100],
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "Select Restaurant Owner",
                  style: TextStyle(fontWeight: FontWeight.w900, fontSize: 14),
                ),
                const SizedBox(height: 12),

                // Search bar for owners
                TextField(
                  onChanged: (value) => setState(() => _ownerSearchQuery = value),
                  decoration: InputDecoration(
                    hintText: "Search by name, email, or ID...",
                    prefixIcon: const Icon(Icons.search),
                    filled: true,
                    fillColor: Colors.white,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(30),
                      borderSide: BorderSide.none,
                    ),
                  ),
                ),

                const SizedBox(height: 12),

                // Owners dropdown/list
                FutureBuilder<List<dynamic>>(
                  future: _allUsersFuture,
                  builder: (context, snap) {
                    if (snap.connectionState == ConnectionState.waiting) {
                      return const Center(
                        child: Padding(
                          padding: EdgeInsets.all(16),
                          child: CircularProgressIndicator(),
                        ),
                      );
                    }

                    if (snap.hasError) {
                      return Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.red[50],
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          "Error loading owners: ${snap.error}",
                          style: const TextStyle(color: Colors.red),
                        ),
                      );
                    }

                    if (_filteredOwners.isEmpty) {
                      return Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Text(
                          "No restaurant owners found",
                          style: TextStyle(color: Colors.grey),
                        ),
                      );
                    }

                    return Container(
                      constraints: const BoxConstraints(maxHeight: 200),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: Colors.grey[300]!),
                      ),
                      child: ListView.separated(
                        shrinkWrap: true,
                        itemCount: _filteredOwners.length,
                        separatorBuilder: (_, __) => const Divider(height: 1),
                        itemBuilder: (context, i) {
                          final owner = (_filteredOwners[i] as Map).cast<String, dynamic>();
                          final id = (owner["userId"] ?? owner["id"] as num).toInt();
                          final name = (owner["fullName"] ?? "Owner").toString();
                          final email = (owner["email"] ?? "").toString();
                          final isSelected = _selectedOwnerId == id;

                          return ListTile(
                            selected: isSelected,
                            selectedTileColor: AppTheme.primaryOrange.withOpacity(0.1),
                            leading: CircleAvatar(
                              backgroundColor: isSelected
                                  ? AppTheme.primaryOrange
                                  : Colors.grey[300],
                              child: Icon(
                                Icons.store,
                                color: isSelected ? Colors.white : Colors.grey[700],
                                size: 20,
                              ),
                            ),
                            title: Text(
                              name,
                              style: TextStyle(
                                fontWeight: FontWeight.w900,
                                color: isSelected ? AppTheme.primaryOrange : null,
                              ),
                            ),
                            subtitle: Text(
                              "$email (ID: $id)",
                              style: const TextStyle(fontSize: 12),
                            ),
                            trailing: isSelected
                                ? const Icon(Icons.check_circle, color: AppTheme.primaryOrange)
                                : null,
                            onTap: () => _loadRestaurants(id),
                          );
                        },
                      ),
                    );
                  },
                ),
              ],
            ),
          ),

          // Restaurants list
          Expanded(
            child: _restaurantsFuture == null
                ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.store_outlined, size: 64, color: Colors.grey[400]),
                  const SizedBox(height: 16),
                  Text(
                    "Select an owner to view restaurants",
                    style: TextStyle(
                      fontWeight: FontWeight.w900,
                      color: Colors.grey[700],
                    ),
                  ),
                ],
              ),
            )
                : FutureBuilder<List<dynamic>>(
              future: _restaurantsFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }

                if (snap.hasError) {
                  // Check if it's a "no restaurants" error vs actual error
                  final errorMsg = snap.error.toString().toLowerCase();
                  final isEmptyError = errorMsg.contains('no restaurants') ||
                      errorMsg.contains('not found') ||
                      errorMsg.contains('empty');

                  if (isEmptyError) {
                    return Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.store_outlined, size: 64, color: Colors.grey[400]),
                          const SizedBox(height: 16),
                          Text(
                            "No restaurants yet",
                            style: TextStyle(
                              fontWeight: FontWeight.w900,
                              color: Colors.grey[700],
                              fontSize: 18,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            "This owner hasn't created any restaurants",
                            style: TextStyle(color: Colors.grey[600]),
                            textAlign: TextAlign.center,
                          ),
                        ],
                      ),
                    );
                  }

                  // Actual error
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.error_outline, size: 48, color: Colors.red),
                        const SizedBox(height: 16),
                        const Text(
                          "Error loading restaurants",
                          style: TextStyle(fontWeight: FontWeight.w900),
                        ),
                        const SizedBox(height: 8),
                        Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 32),
                          child: Text(
                            snap.error.toString(),
                            style: const TextStyle(color: Colors.red),
                            textAlign: TextAlign.center,
                          ),
                        ),
                        const SizedBox(height: 16),
                        ElevatedButton(
                          onPressed: () {
                            if (_selectedOwnerId != null) {
                              _loadRestaurants(_selectedOwnerId!);
                            }
                          },
                          child: const Text("Retry"),
                        ),
                      ],
                    ),
                  );
                }

                final restaurants = snap.data ?? [];
                if (restaurants.isEmpty) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.inbox_outlined, size: 64, color: Colors.grey[400]),
                        const SizedBox(height: 16),
                        Text(
                          "No restaurants found",
                          style: TextStyle(
                            fontWeight: FontWeight.w900,
                            color: Colors.grey[700],
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          "This owner hasn't created any restaurants",
                          style: TextStyle(color: Colors.grey[600]),
                        ),
                      ],
                    ),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: restaurants.length,
                  itemBuilder: (context, i) {
                    final r = (restaurants[i] as Map).cast<String, dynamic>();
                    final id = r["id"];
                    final name = (r["name"] ?? "Restaurant").toString();
                    final cuisine = (r["cuisine"] ?? "").toString();
                    final address = (r["address"] ?? "").toString();
                    final rating = r["rating"];
                    final active = (r["isActive"] ?? false) == true;

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
                                  width: 50,
                                  height: 50,
                                  decoration: BoxDecoration(
                                    color: AppTheme.primaryOrange.withOpacity(0.12),
                                    borderRadius: BorderRadius.circular(14),
                                  ),
                                  child: const Icon(
                                    Icons.store_rounded,
                                    color: AppTheme.primaryOrange,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        name,
                                        style: const TextStyle(fontWeight: FontWeight.w900),
                                      ),
                                      const SizedBox(height: 4),
                                      Text(
                                        cuisine,
                                        style: TextStyle(
                                          color: Colors.grey[700],
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                      if (address.isNotEmpty) ...[
                                        const SizedBox(height: 4),
                                        Text(
                                          address,
                                          style: TextStyle(
                                            color: Colors.grey[600],
                                            fontSize: 12,
                                          ),
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ],
                                    ],
                                  ),
                                ),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.end,
                                  children: [
                                    if (rating != null) ...[
                                      Row(
                                        children: [
                                          const Icon(Icons.star, color: Colors.amber, size: 16),
                                          const SizedBox(width: 4),
                                          Text(
                                            rating.toString(),
                                            style: const TextStyle(fontWeight: FontWeight.w900),
                                          ),
                                        ],
                                      ),
                                      const SizedBox(height: 8),
                                    ],
                                    _pill(
                                      text: active ? "ACTIVE" : "INACTIVE",
                                      color: active ? const Color(0xFF2E7D32) : const Color(0xFFC62828),
                                    ),
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
                                Text(
                                  "ID: $id",
                                  style: TextStyle(
                                    color: Colors.grey[600],
                                    fontSize: 12,
                                    fontWeight: FontWeight.w700,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                );
              },
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
      style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: 12),
    ),
  );

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