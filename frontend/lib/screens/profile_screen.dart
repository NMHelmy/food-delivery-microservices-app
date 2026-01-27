import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../providers/auth_provider.dart';
import '../services/profile_service.dart';
import '../services/token_service.dart';

import 'welcome_screen.dart';
import 'delivery_addresses_screen.dart';
import 'payment_methods_screen.dart';
import 'notifications_screen.dart';
import 'help_support_screen.dart';
import 'settings_screen.dart';
import 'orders_screen.dart';
import 'address_screen.dart';

import 'admin_home_screen.dart';
import 'owner_home_screen.dart';
import 'driver_home_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  late Future<Map<String, dynamic>> _profileFuture;

  @override
  void initState() {
    super.initState();
    _profileFuture = ProfileService.fetchProfile();
  }

  Future<void> _logout() async {
    await TokenService.clear();
    if (!mounted) return;

    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (_) => const WelcomeScreen()),
          (_) => false,
    );
  }

  Widget _sectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        text,
        style: TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.w600,
          color: Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
        ),
      ),
    );
  }

  Widget _menuTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    final theme = Theme.of(context);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(18),
      child: Container(
        margin: const EdgeInsets.only(bottom: 14),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(18),
          boxShadow: [
            BoxShadow(
              color: theme.shadowColor.withOpacity(0.08),
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
                color: theme.colorScheme.primary.withOpacity(0.12),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: theme.colorScheme.primary),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 15,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: TextStyle(
                      fontSize: 12,
                      color: theme.colorScheme.onSurface.withOpacity(0.6),
                    ),
                  ),
                ],
              ),
            ),
            Icon(
              Icons.chevron_right,
              color: theme.colorScheme.onSurface.withOpacity(0.4),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primary = theme.colorScheme.primary;

    final role = context.watch<AuthProvider>().role; // CUSTOMER / ADMIN / RESTAURANTOWNER / DELIVERYDRIVER [file:3][file:6]
    final isCustomer = role == "CUSTOMER";
    final isAdmin = role == "ADMIN";
    final isOwner = role == "RESTAURANTOWNER";
    final isDriver = role == "DELIVERYDRIVER";

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
      appBar: AppBar(
        title: Text(isCustomer ? "Profile" : "Account"),
        centerTitle: true,
        backgroundColor: theme.appBarTheme.backgroundColor,
        elevation: 0,
      ),
      body: FutureBuilder<Map<String, dynamic>>(
        future: _profileFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(
              child: Text(
                snapshot.error.toString(),
                style: const TextStyle(color: Colors.red),
              ),
            );
          }

          final data = snapshot.data!;

          return SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 30),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Identity card
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    color: primary.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(26),
                  ),
                  child: Column(
                    children: [
                      Container(
                        width: 80,
                        height: 80,
                        decoration: BoxDecoration(
                          color: primary,
                          borderRadius: BorderRadius.circular(26),
                        ),
                        child: const Icon(Icons.person, color: Colors.white, size: 40),
                      ),
                      const SizedBox(height: 16),
                      Text(
                        data['fullName'] ?? "",
                        style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w700),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 8),
                      Text(
                        data['email'] ?? "",
                        style: TextStyle(fontSize: 13, color: theme.colorScheme.onSurface.withOpacity(0.7)),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        data['phoneNumber'] ?? "",
                        style: TextStyle(fontSize: 13, color: theme.colorScheme.onSurface.withOpacity(0.7)),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 10),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        decoration: BoxDecoration(
                          color: Colors.black.withOpacity(0.06),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Text(
                          "Role: $role",
                          style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 12),
                        ),
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 36),

                // Account section
                _sectionTitle("Account"),

                // Role-specific main tile (non-customer)
                if (isAdmin)
                  _menuTile(
                    icon: Icons.admin_panel_settings,
                    title: "Admin Panel",
                    subtitle: "Manage system operations",
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const AdminHomeScreen()),
                    ),
                  ),

                if (isOwner)
                  _menuTile(
                    icon: Icons.store,
                    title: "Owner Panel",
                    subtitle: "Manage restaurants and menus",
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const OwnerHomeScreen()),
                    ),
                  ),

                if (isDriver)
                  _menuTile(
                    icon: Icons.delivery_dining,
                    title: "Driver Panel",
                    subtitle: "Manage deliveries and status",
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const DriverHomeScreen()),
                    ),
                  ),

                // Customer-only tiles
                if (isCustomer)
                  _menuTile(
                    icon: Icons.receipt_long,
                    title: "Orders",
                    subtitle: "View your order history",
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const OrdersScreen()),
                    ),
                  ),

                if (isCustomer)
                  _menuTile(
                    icon: Icons.location_on_outlined,
                    title: "Delivery Addresses",
                    subtitle: "Manage your addresses",
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const AddressesScreen()),
                    ),
                  ),

                if (isCustomer)
                  _menuTile(
                    icon: Icons.credit_card,
                    title: "Payment Methods",
                    subtitle: "Cards and wallets",
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const PaymentMethodsScreen()),
                    ),
                  ),

                const SizedBox(height: 28),

                // Preferences
                _sectionTitle("Preferences"),
                _menuTile(
                  icon: Icons.notifications_outlined,
                  title: "Notifications",
                  subtitle: "Manage your alerts",
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const NotificationsScreen()),
                  ),
                ),
                _menuTile(
                  icon: Icons.settings_outlined,
                  title: "Settings",
                  subtitle: "App preferences",
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const SettingsScreen()),
                  ),
                ),

                const SizedBox(height: 28),

                // Support
                _sectionTitle("Support"),
                _menuTile(
                  icon: Icons.help_outline,
                  title: "Help & Support",
                  subtitle: "Get help anytime",
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const HelpSupportScreen()),
                  ),
                ),

                const SizedBox(height: 40),

                // Logout
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: _logout,
                    icon: const Icon(Icons.logout),
                    label: const Text("Logout"),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
