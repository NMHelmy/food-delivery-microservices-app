import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/address.dart';
import '../providers/cart_provider.dart';
import '../services/address_service.dart';
import '../services/order_service.dart';
import '../theme/app_theme.dart';

class CheckoutScreen extends StatefulWidget {
  const CheckoutScreen({super.key});

  @override
  State<CheckoutScreen> createState() => _CheckoutScreenState();
}

class _CheckoutScreenState extends State<CheckoutScreen> {
  late Future<List<Address>> _addressesFuture;
  int? _selectedAddressId;
  final TextEditingController _notesController = TextEditingController();

  bool _placingOrder = false;

  @override
  void initState() {
    super.initState();
    _addressesFuture = AddressService.getMyAddresses();
  }

  @override
  void dispose() {
    _notesController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final cartProvider = context.watch<CartProvider>();
    final cart = cartProvider.cart;

    if (cart == null || cart.items.isEmpty) {
      return Scaffold(
        appBar: AppBar(title: const Text("Checkout")),
        body: const Center(child: Text("Your cart is empty.")),
      );
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF6F6F6),
      appBar: AppBar(
        title: const Text(
          "Checkout",
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: FutureBuilder<List<Address>>(
        future: _addressesFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  "Failed to load addresses.\n${snapshot.error}",
                  textAlign: TextAlign.center,
                ),
              ),
            );
          }

          final addresses = snapshot.data ?? [];

          if (_selectedAddressId == null && addresses.isNotEmpty) {
            final def = addresses.where((a) => a.isDefault).toList();
            _selectedAddressId = def.isNotEmpty ? def.first.id : addresses.first.id;
          }

          return Stack(
            children: [
              ListView(
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 120),
                children: [
                  _sectionTitle("Delivery address"),
                  const SizedBox(height: 8),
                  if (addresses.isEmpty)
                    _card(
                      child: const Text(
                        "No saved addresses found. Please add one first.",
                        style: TextStyle(fontWeight: FontWeight.w700),
                      ),
                    )
                  else
                    _card(
                      child: Column(
                        children: addresses.map((a) {
                          return RadioListTile<int>(
                            value: a.id,
                            groupValue: _selectedAddressId,
                            activeColor: AppTheme.primaryOrange,
                            title: Row(
                              children: [
                                Expanded(
                                  child: Text(
                                    a.displayTitle(),
                                    style: const TextStyle(fontWeight: FontWeight.w900),
                                  ),
                                ),
                                if (a.isDefault)
                                  Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 8,
                                      vertical: 4,
                                    ),
                                    decoration: BoxDecoration(
                                      color: AppTheme.primaryOrange.withOpacity(0.12),
                                      borderRadius: BorderRadius.circular(12),
                                    ),
                                    child: const Text(
                                      "Default",
                                      style: TextStyle(
                                        color: AppTheme.primaryOrange,
                                        fontWeight: FontWeight.w900,
                                        fontSize: 12,
                                      ),
                                    ),
                                  ),
                              ],
                            ),
                            subtitle: Text(
                              a.displayDetails(),
                              style: TextStyle(color: Colors.grey[700]),
                            ),
                            onChanged:
                            _placingOrder ? null : (v) => setState(() => _selectedAddressId = v),
                          );
                        }).toList(),
                      ),
                    ),
                  const SizedBox(height: 12),

                  _sectionTitle("Special instructions"),
                  const SizedBox(height: 8),
                  _card(
                    child: TextField(
                      controller: _notesController,
                      maxLines: 3,
                      decoration: const InputDecoration(
                        hintText: "Add delivery notes (optional)",
                        border: InputBorder.none,
                      ),
                    ),
                  ),

                  const SizedBox(height: 12),
                  _sectionTitle("Order summary"),
                  const SizedBox(height: 8),
                  _card(
                    child: Column(
                      children: [
                        _row("Items", cart.totalItems.toString()),
                        const SizedBox(height: 10),
                        _row("Subtotal", "${cart.subtotal.toStringAsFixed(2)} EGP"),
                        const SizedBox(height: 10),
                        Text(
                          "Delivery fee, tax, and total will be calculated after placing the order.",
                          style: TextStyle(color: Colors.grey[700], fontSize: 12.5),
                        ),
                      ],
                    ),
                  ),
                ],
              ),

              Positioned(
                left: 0,
                right: 0,
                bottom: 0,
                child: SafeArea(
                  top: false,
                  child: Container(
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 12),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.10),
                          blurRadius: 18,
                          offset: const Offset(0, -6),
                        ),
                      ],
                    ),
                    child: SizedBox(
                      height: 48,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppTheme.primaryOrange,
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(14),
                          ),
                        ),
                        onPressed: _placingOrder
                            ? null
                            : () async {
                          if (_selectedAddressId == null) {
                            _showAppSnack(
                              context,
                              "Select a delivery address first",
                              type: AppSnackType.error,
                            );
                            return;
                          }

                          setState(() => _placingOrder = true);

                          try {
                            final orderMap = await OrdersService.checkoutCart(
                              deliveryAddressId: _selectedAddressId!,
                              specialInstructions: _notesController.text.trim().isEmpty
                                  ? null
                                  : _notesController.text.trim(),
                            );

                            // Map access, not order.id
                            final orderId = orderMap['id'];

                            if (!context.mounted) return;

                            await context.read<CartProvider>().loadCart();

                            if (!context.mounted) return;

                            _showAppSnack(
                              context,
                              "Order placed (#$orderId)",
                              type: AppSnackType.success,
                            );

                            Navigator.pop(context); // back to cart
                          } catch (e) {
                            if (!context.mounted) return;
                            _showAppSnack(
                              context,
                              e.toString(),
                              type: AppSnackType.error,
                            );
                          } finally {
                            if (mounted) setState(() => _placingOrder = false);
                          }
                        },
                        child: Text(
                          _placingOrder ? "Placing order..." : "Place order",
                          style: const TextStyle(fontWeight: FontWeight.w900),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _sectionTitle(String text) {
    return Text(
      text,
      style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
    );
  }

  Widget _card({required Widget child}) {
    return Container(
      padding: const EdgeInsets.all(14),
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
      child: child,
    );
  }

  Widget _row(String left, String right) {
    return Row(
      children: [
        Text(left, style: const TextStyle(fontWeight: FontWeight.w800)),
        const Spacer(),
        Text(right, style: const TextStyle(fontWeight: FontWeight.w900)),
      ],
    );
  }
}

enum AppSnackType { success, error, info }

void _showAppSnack(BuildContext context, String text,
    {AppSnackType type = AppSnackType.info}) {
  Color bg;
  IconData icon;

  switch (type) {
    case AppSnackType.success:
      bg = const Color(0xFF2E7D32);
      icon = Icons.check_circle_outline;
      break;
    case AppSnackType.error:
      bg = const Color(0xFFC62828);
      icon = Icons.error_outline;
      break;
    case AppSnackType.info:
      bg = const Color(0xFF2D2D2D);
      icon = Icons.info_outline;
      break;
  }

  ScaffoldMessenger.of(context).clearSnackBars();
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      behavior: SnackBarBehavior.floating,
      backgroundColor: bg,
      elevation: 10,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      margin: const EdgeInsets.fromLTRB(16, 0, 16, 16),
      content: Row(
        children: [
          Icon(icon, color: Colors.white),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              text,
              maxLines: 3,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontWeight: FontWeight.w800),
            ),
          ),
        ],
      ),
    ),
  );
}
