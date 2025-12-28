import 'package:flutter/material.dart';
import '../services/payment_service.dart';
import '../theme/app_theme.dart';
import 'payment_result_screen.dart';

class ChoosePaymentScreen extends StatefulWidget {
  final int orderId;

  const ChoosePaymentScreen({
    super.key,
    required this.orderId,
  });

  @override
  State<ChoosePaymentScreen> createState() => _ChoosePaymentScreenState();
}

class _ChoosePaymentScreenState extends State<ChoosePaymentScreen> {
  String? _selected; // "CASH" | "CARD"
  bool _loading = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF6F6F6),
      appBar: AppBar(
        title: const Text("Payment", style: TextStyle(fontWeight: FontWeight.w900)),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text(
            "Choose payment method",
            style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
          ),
          const SizedBox(height: 12),

          _methodTile(
            title: "Cash on delivery",
            subtitle: "Pay when the order arrives",
            value: "CASH",
          ),
          const SizedBox(height: 10),
          _methodTile(
            title: "Card",
            subtitle: "Pay now (simulated confirmation)",
            value: "CARD",
          ),

          const SizedBox(height: 20),
          SizedBox(
            height: 48,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: AppTheme.primaryOrange,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
              ),
              onPressed: _loading || _selected == null ? null : _continue,
              child: Text(
                _loading ? "Processing..." : "Continue",
                style: const TextStyle(fontWeight: FontWeight.w900),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _methodTile({
    required String title,
    required String subtitle,
    required String value,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: RadioListTile<String>(
        value: value,
        groupValue: _selected,
        activeColor: AppTheme.primaryOrange,
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
        subtitle: Text(subtitle),
        onChanged: (v) => setState(() => _selected = v),
      ),
    );
  }

  Future<void> _continue() async {
    setState(() => _loading = true);

    try {
      if (_selected == "CASH") {
        if (!mounted) return;
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (_) => PaymentResultScreen(
              success: true,
              orderId: widget.orderId,
              title: "Order placed",
              message: "Pay cash on delivery.",
            ),
          ),
        );
        return;
      }

      // CARD => create payment then confirm it (simulated success)
      final created = await PaymentService.createPayment(
        orderId: widget.orderId,
        paymentMethod: "CARD",
      );

      final paymentId = (created["paymentId"] as num).toInt();

      await PaymentService.confirmPayment(paymentId: paymentId);

      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => PaymentResultScreen(
            success: true,
            orderId: widget.orderId,
            title: "Payment successful",
            message: "Your order is now paid.",
          ),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => PaymentResultScreen(
            success: false,
            orderId: widget.orderId,
            title: "Payment failed",
            message: e.toString(),
          ),
        ),
      );
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}
