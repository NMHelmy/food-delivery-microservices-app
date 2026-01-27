import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/admin_service.dart';
import 'admin_user_details_screen.dart';

class AdminUsersScreen extends StatefulWidget {
  const AdminUsersScreen({super.key});

  @override
  State<AdminUsersScreen> createState() => _AdminUsersScreenState();
}

class _AdminUsersScreenState extends State<AdminUsersScreen> {
  late Future<List<dynamic>> _future;
  String _q = "";

  @override
  void initState() {
    super.initState();
    _future = AdminService.getUsers();
  }

  Future<void> _refresh() async {
    setState(() => _future = AdminService.getUsers());
    await _future;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Users", style: TextStyle(fontWeight: FontWeight.w900))),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 10),
            child: Container(
              height: 52,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(30),
                boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.06), blurRadius: 12)],
              ),
              padding: const EdgeInsets.symmetric(horizontal: 18),
              child: TextField(
                onChanged: (v) => setState(() => _q = v.trim().toLowerCase()),
                decoration: const InputDecoration(
                  icon: Icon(Icons.search, color: Colors.grey),
                  hintText: "Search name/email/phone/id",
                  border: InputBorder.none,
                ),
              ),
            ),
          ),
          Expanded(
            child: FutureBuilder<List<dynamic>>(
              future: _future,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) return const Center(child: CircularProgressIndicator());
                if (snap.hasError) return Center(child: Text(snap.error.toString(), style: const TextStyle(color: Colors.red)));

                final all = snap.data ?? [];
                final list = all.where((u) {
                  final m = (u as Map).cast<String, dynamic>();
                  final id = (m["userId"] ?? m["id"] ?? "").toString().toLowerCase();
                  final name = (m["fullName"] ?? "").toString().toLowerCase();
                  final email = (m["email"] ?? "").toString().toLowerCase();
                  final phone = (m["phoneNumber"] ?? m["phone"] ?? "").toString().toLowerCase();
                  return id.contains(_q) || name.contains(_q) || email.contains(_q) || phone.contains(_q);
                }).toList();

                if (list.isEmpty) return const Center(child: Text("No users found"));

                return RefreshIndicator(
                  onRefresh: _refresh,
                  child: ListView.builder(
                    padding: const EdgeInsets.fromLTRB(16, 6, 16, 16),
                    itemCount: list.length,
                    itemBuilder: (context, i) {
                      final u = (list[i] as Map).cast<String, dynamic>();
                      final userId = (u["userId"] ?? u["id"] as num).toInt();
                      final role = (u["role"] ?? "-").toString();

                      return InkWell(
                        borderRadius: BorderRadius.circular(16),
                        onTap: () => Navigator.push(
                          context,
                          MaterialPageRoute(builder: (_) => AdminUserDetailsScreen(userId: userId)),
                        ),
                        child: Container(
                          margin: const EdgeInsets.only(bottom: 12),
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(16),
                            boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 6))],
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
                                child: const Icon(Icons.person, color: AppTheme.primaryOrange),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text((u["fullName"] ?? "User").toString(),
                                        style: const TextStyle(fontWeight: FontWeight.w900)),
                                    const SizedBox(height: 4),
                                    Text((u["email"] ?? "").toString(),
                                        style: TextStyle(color: Colors.grey[700], fontWeight: FontWeight.w700)),
                                    const SizedBox(height: 4),
                                    Text("ID: $userId", style: TextStyle(color: Colors.grey[700])),
                                  ],
                                ),
                              ),
                              _RoleChip(role: role),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _RoleChip extends StatelessWidget {
  final String role;
  const _RoleChip({required this.role});

  @override
  Widget build(BuildContext context) {
    Color bg;
    switch (role) {
      case "ADMIN":
        bg = const Color(0xFF6A1B9A);
        break;
      case "RESTAURANT_OWNER":
        bg = const Color(0xFF1565C0);
        break;
      case "DELIVERY_DRIVER":
        bg = const Color(0xFF2E7D32);
        break;
      default:
        bg = const Color(0xFF424242);
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(color: bg.withOpacity(0.12), borderRadius: BorderRadius.circular(14)),
      child: Text(role, style: TextStyle(color: bg, fontWeight: FontWeight.w900, fontSize: 12)),
    );
  }
}
