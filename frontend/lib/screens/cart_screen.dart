import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../providers/cart_provider.dart';
import '../providers/menu_item_image_provider.dart';
import '../theme/app_theme.dart';
import 'checkout_screen.dart';

class CartScreen extends StatefulWidget {
  const CartScreen({super.key});

  @override
  State<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends State<CartScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CartProvider>().loadCart();
    });
  }

  @override
  Widget build(BuildContext context) {
    final cartProvider = context.watch<CartProvider>();
    final cart = cartProvider.cart;

    return Scaffold(
      backgroundColor: const Color(0xFFF6F6F6),
      appBar: AppBar(
        title: const Text(
          "Your Cart",
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
        actions: [
          if ((cart?.items.isNotEmpty ?? false))
            TextButton(
              onPressed: cartProvider.loading
                  ? null
                  : () async {
                final ok = await _confirmClearCart(context);
                if (ok != true) return;

                try {
                  await context.read<CartProvider>().clear();
                  if (!context.mounted) return;
                  _showAppSnack(context, "Cart cleared",
                      type: AppSnackType.success);
                } catch (e) {
                  if (!context.mounted) return;
                  _showAppSnack(context, e.toString(),
                      type: AppSnackType.error);
                }
              },
              child: const Text(
                "Clear",
                style: TextStyle(
                  color: AppTheme.primaryOrange,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ),
        ],
      ),
      body: cartProvider.loading
          ? const Center(child: CircularProgressIndicator())
          : (cart == null || cart.items.isEmpty)
          ? _emptyCart()
          : Stack(
        children: [
          ListView.builder(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 120),
            itemCount: cart.items.length,
            itemBuilder: (context, index) {
              final item = cart.items[index];

              // Warm-up image cache (safe; provider dedupes)
              context.read<MenuItemImageProvider>().warmUp(
                restaurantId: cart.restaurantId,
                menuItemId: item.menuItemId,
              );

              final imgUrl =
              context.watch<MenuItemImageProvider>().getImageUrl(
                restaurantId: cart.restaurantId,
                menuItemId: item.menuItemId,
              );

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
                    ),
                  ],
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // LEFT: image + item total under it
                    Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        ClipRRect(
                          borderRadius: BorderRadius.circular(14),
                          child: SizedBox(
                            width: 74,
                            height: 74,
                            child: (imgUrl != null && imgUrl.isNotEmpty)
                                ? Image.network(
                              imgUrl,
                              fit: BoxFit.cover,
                              errorBuilder: (_, __, ___) =>
                                  _cartImgPlaceholder(),
                            )
                                : _cartImgPlaceholder(),
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          "Item total:",
                          style: TextStyle(
                            color: Colors.grey[700],
                            fontSize: 11.5,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          "${item.itemTotal.toStringAsFixed(2)} EGP",
                          style: const TextStyle(
                            fontWeight: FontWeight.w900,
                            fontSize: 12.5,
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(width: 12),

                    // MIDDLE: name + customizations + unit price
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            item.itemName,
                            style: const TextStyle(
                              fontWeight: FontWeight.w900,
                              fontSize: 15,
                            ),
                          ),
                          if ((item.customizations ?? '')
                              .trim()
                              .isNotEmpty) ...[
                            const SizedBox(height: 6),
                            Text(
                              item.customizations!,
                              style: TextStyle(
                                color: Colors.grey[600],
                                fontSize: 12.5,
                                height: 1.25,
                              ),
                            ),
                          ],
                          const SizedBox(height: 10),
                          Text(
                            "${item.price.toStringAsFixed(2)} EGP",
                            style: const TextStyle(
                              color: AppTheme.primaryOrange,
                              fontWeight: FontWeight.w900,
                            ),
                          ),
                        ],
                      ),
                    ),

                    const SizedBox(width: 10),

                    // RIGHT: qty controls + remove
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Container(
                          decoration: BoxDecoration(
                            color: const Color(0xFFF6F6F6),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              IconButton(
                                icon: const Icon(Icons.remove),
                                onPressed: cartProvider.loading
                                    ? null
                                    : () async {
                                  final newQty =
                                      item.quantity - 1;
                                  try {
                                    if (newQty <= 0) {
                                      await context
                                          .read<CartProvider>()
                                          .removeItem(item.id);
                                    } else {
                                      await context
                                          .read<CartProvider>()
                                          .updateItem(
                                        cartItemId: item.id,
                                        quantity: newQty,
                                        customizations:
                                        item.customizations,
                                      );
                                    }
                                  } catch (e) {
                                    if (!context.mounted) return;
                                    _showAppSnack(
                                      context,
                                      e.toString(),
                                      type: AppSnackType.error,
                                    );
                                  }
                                },
                              ),
                              Text(
                                item.quantity.toString(),
                                style: const TextStyle(
                                  fontWeight: FontWeight.w900,
                                  fontSize: 14,
                                ),
                              ),
                              IconButton(
                                icon: const Icon(Icons.add),
                                onPressed: cartProvider.loading
                                    ? null
                                    : () async {
                                  try {
                                    await context
                                        .read<CartProvider>()
                                        .updateItem(
                                      cartItemId: item.id,
                                      quantity:
                                      item.quantity + 1,
                                      customizations:
                                      item.customizations,
                                    );
                                  } catch (e) {
                                    if (!context.mounted) return;
                                    _showAppSnack(
                                      context,
                                      e.toString(),
                                      type: AppSnackType.error,
                                    );
                                  }
                                },
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 8),
                        TextButton.icon(
                          onPressed: cartProvider.loading
                              ? null
                              : () async {
                            try {
                              await context
                                  .read<CartProvider>()
                                  .removeItem(item.id);
                              if (!context.mounted) return;
                              _showAppSnack(
                                context,
                                "Removed from cart",
                                type: AppSnackType.info,
                              );
                            } catch (e) {
                              if (!context.mounted) return;
                              _showAppSnack(
                                context,
                                e.toString(),
                                type: AppSnackType.error,
                              );
                            }
                          },
                          icon: const Icon(Icons.delete_outline, size: 18),
                          label: const Text("Remove"),
                          style: TextButton.styleFrom(
                            foregroundColor: Colors.red[700],
                            textStyle: const TextStyle(
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              );
            },
          ),

          // Bottom checkout bar
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
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            "Subtotal",
                            style: TextStyle(color: Colors.grey[700]),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            "${cart.subtotal.toStringAsFixed(2)} EGP",
                            style: const TextStyle(
                              fontWeight: FontWeight.w900,
                              fontSize: 16,
                            ),
                          ),
                        ],
                      ),
                    ),
                    SizedBox(
                      height: 48,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppTheme.primaryOrange,
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(14),
                          ),
                        ),
                        onPressed: cartProvider.loading
                            ? null
                            : () async {
                          await Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) =>
                              const CheckoutScreen(),
                            ),
                          );

                          // When returning from checkout, refresh cart state
                          if (!context.mounted) return;
                          await context
                              .read<CartProvider>()
                              .loadCart();
                        },
                        child: const Text(
                          "Checkout",
                          style: TextStyle(fontWeight: FontWeight.w900),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _cartImgPlaceholder() {
    return Container(
      color: AppTheme.primaryOrange.withOpacity(0.12),
      child: const Center(
        child: Icon(
          Icons.fastfood_rounded,
          color: AppTheme.primaryOrange,
          size: 28,
        ),
      ),
    );
  }

  Widget _emptyCart() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(22),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                color: AppTheme.primaryOrange.withOpacity(0.12),
                borderRadius: BorderRadius.circular(18),
              ),
              child: const Icon(
                Icons.shopping_cart_outlined,
                color: AppTheme.primaryOrange,
                size: 36,
              ),
            ),
            const SizedBox(height: 14),
            const Text(
              "Your cart is empty",
              style: TextStyle(
                fontWeight: FontWeight.w900,
                fontSize: 18,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              "Add items from a restaurant to start your order.",
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey[700]),
            ),
          ],
        ),
      ),
    );
  }
}

// ---------- Helpers ----------

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

Future<bool?> _confirmClearCart(BuildContext context) {
  return showDialog<bool>(
    context: context,
    builder: (ctx) {
      return AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text("Clear cart?"),
        content: const Text("This will remove all items from your cart."),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text("Cancel"),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppTheme.primaryOrange,
              foregroundColor: Colors.white,
            ),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text("Clear"),
          ),
        ],
      );
    },
  );
}
