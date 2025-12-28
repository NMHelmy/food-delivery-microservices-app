import 'package:flutter/material.dart';
import 'app_tab.dart';

import '../screens/home_screen.dart';
import '../screens/cart_screen.dart';
import '../screens/orders_screen.dart';
import '../screens/profile_screen.dart';

import '../screens/admin_home_screen.dart';
import '../screens/owner_home_screen.dart';
import '../screens/driver_home_screen.dart';

List<AppTab> tabsForRole(String role) {
  switch (role) {
    case "ADMIN":
      return [
        AppTab(label: "Home", icon: Icons.home, page: const HomeScreen()),
        AppTab(label: "Admin", icon: Icons.admin_panel_settings, page: const AdminHomeScreen()),
        AppTab(label: "Profile", icon: Icons.person, page: const ProfileScreen()),
      ];

    case "RESTAURANT_OWNER":
      return [
        AppTab(label: "Home", icon: Icons.home, page: const HomeScreen()),
        AppTab(label: "Owner", icon: Icons.store, page: const OwnerHomeScreen()),
        AppTab(label: "Profile", icon: Icons.person, page: const ProfileScreen()),
      ];

    case "DELIVERY_DRIVER":
      return [
        AppTab(label: "Home", icon: Icons.home, page: const HomeScreen()),
        AppTab(label: "Driver", icon: Icons.delivery_dining, page: const DriverHomeScreen()),
        AppTab(label: "Profile", icon: Icons.person, page: const ProfileScreen()),
      ];

    default: // CUSTOMER
      return [
        AppTab(label: "Home", icon: Icons.home, page: const HomeScreen()),
        AppTab(label: "Cart", icon: Icons.shopping_cart, page: const CartScreen()),
        AppTab(label: "Orders", icon: Icons.receipt_long, page: const OrdersScreen()),
        AppTab(label: "Profile", icon: Icons.person, page: const ProfileScreen()),
      ];
  }
}
