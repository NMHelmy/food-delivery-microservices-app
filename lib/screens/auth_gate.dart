import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../services/token_service.dart';
import '../providers/cart_provider.dart';
import 'welcome_screen.dart';
import 'main_navigation_screen.dart';

class AuthGate extends StatefulWidget {
  const AuthGate({super.key});

  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkAuth();
    });
  }

  Future<void> _checkAuth() async {
    final token = await TokenService.getToken();
    if (!mounted) return;

    final cartProvider = context.read<CartProvider>();

    if (token != null && token.isNotEmpty) {
      // ✅ new (or existing) session: fetch correct cart for this token
      await cartProvider.loadCart();

      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => const MainNavigationScreen()),
      );
    } else {
      // logged out: remove any previous user's cart from memory/UI
      cartProvider.clearLocal();

      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => const WelcomeScreen()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(child: CircularProgressIndicator()),
    );
  }
}
