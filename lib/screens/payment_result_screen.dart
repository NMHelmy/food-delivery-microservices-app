import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import 'order_details_screen.dart';
import 'main_navigation_screen.dart';

class PaymentResultScreen extends StatelessWidget {
  final bool success;
  final int orderId;
  final String title;
  final String message;

  const PaymentResultScreen({
    super.key,
    required this.success,
    required this.orderId,
    required this.title,
    required this.message,
  });

  @override
  Widget build(BuildContext context) {
    final color = success ? const Color(0xFF2E7D32) : const Color(0xFFC62828);

    return Scaffold(
      appBar: AppBar(
        title: const Text("Order", style: TextStyle(fontWeight: FontWeight.w900)),
        automaticallyImplyLeading: false,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const SizedBox(height: 18),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: color.withOpacity(0.08),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: TextStyle(fontWeight: FontWeight.w900, fontSize: 18, color: color)),
                  const SizedBox(height: 8),
                  Text(message, style: const TextStyle(fontWeight: FontWeight.w700)),
                  const SizedBox(height: 8),
                  Text("Order #$orderId", style: TextStyle(color: Colors.grey[700])),
                ],
              ),
            ),

            const Spacer(),
            SafeArea(
              top: false,
              child: Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Column(
                  children: [
                    SizedBox(
                      height: 48,
                      width: double.infinity,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppTheme.primaryOrange,
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                        ),
                        onPressed: () {
                          Navigator.pushReplacement(
                            context,
                            MaterialPageRoute(
                              builder: (_) => OrderDetailsScreen(orderId: orderId),
                            ),
                          );
                        },
                        child: const Text("Track order", style: TextStyle(fontWeight: FontWeight.w900)),
                      ),
                    ),
                    const SizedBox(height: 10),
                    SizedBox(
                      height: 48,
                      width: double.infinity,
                      child: OutlinedButton(
                        style: OutlinedButton.styleFrom(
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                        ),
                        onPressed: () {
                          Navigator.pushAndRemoveUntil(
                            context,
                            MaterialPageRoute(builder: (_) => const MainNavigationScreen()),
                                (route) => false,
                          );
                        },
                        child: const Text("Back to home", style: TextStyle(fontWeight: FontWeight.w900)),
                      ),
                    ),
                  ],
                ),
              ),
            ),

          ],
        ),
      ),
    );
  }
}
