import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/restaurant.dart';
import '../models/menu_item.dart';
import '../services/menu_service.dart';
import '../widgets/menu_item_card.dart';
import '../theme/app_theme.dart';
import '../providers/cart_provider.dart';
import 'cart_screen.dart';

class RestaurantDetailsScreen extends StatefulWidget {
  final Restaurant restaurant;

  const RestaurantDetailsScreen({super.key, required this.restaurant});

  @override
  State<RestaurantDetailsScreen> createState() => _RestaurantDetailsScreenState();
}

class _RestaurantDetailsScreenState extends State<RestaurantDetailsScreen> {
  late Future<List<MenuItem>> _menuFuture;

  final ScrollController _scrollController = ScrollController();

  int _selectedCategoryIndex = 0;
  List<String> _categories = const ["All"];

  final Map<String, GlobalKey> _categoryHeaderKeys = {};
  bool _programmaticScroll = false;

  @override
  void initState() {
    super.initState();
    _menuFuture = MenuService.getMenuItems(widget.restaurant.id);
    _scrollController.addListener(_onScroll);

    // Optional: ensure cart is loaded so the bar can appear immediately
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CartProvider>().loadCart();
    });
  }

  @override
  void dispose() {
    _scrollController.removeListener(_onScroll);
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final cartProvider = context.watch<CartProvider>();
    final cart = cartProvider.cart;

    final hasCartItems = cart != null && cart.items.isNotEmpty;
    final cartFromSameRestaurant =
        hasCartItems && cart.restaurantId == widget.restaurant.id;

    return Scaffold(
      backgroundColor: const Color(0xFFF6F6F6),
      body: Stack(
        children: [
          FutureBuilder<List<MenuItem>>(
            future: _menuFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(child: CircularProgressIndicator());
              }

              if (snapshot.hasError) {
                return const Center(child: Text("Failed to load menu"));
              }

              final menuItems = snapshot.data ?? [];
              _prepareCategories(menuItems);

              // Add bottom padding so list doesn't hide behind the cart bar
              final bottomPadding = hasCartItems ? 98.0 : 16.0;

              return CustomScrollView(
                controller: _scrollController,
                slivers: [
                  _buildHeader(),
                  _buildPinnedCategories(),
                  ..._buildMenuByCategorySlivers(menuItems),
                  SliverToBoxAdapter(child: SizedBox(height: bottomPadding)),
                ],
              );
            },
          ),

          // ✅ Sticky View Cart bar
          if (hasCartItems)
            Positioned(
              left: 0,
              right: 0,
              bottom: 0,
              child: SafeArea(
                top: false,
                child: Container(
                  padding: const EdgeInsets.fromLTRB(16, 10, 16, 12),
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
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      if (!cartFromSameRestaurant)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 8),
                          child: Row(
                            children: [
                              Icon(Icons.info_outline,
                                  color: Colors.red[700], size: 18),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  "Your cart has items from another restaurant.",
                                  style: TextStyle(
                                    color: Colors.red[700],
                                    fontWeight: FontWeight.w800,
                                    fontSize: 12.5,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      SizedBox(
                        height: 52,
                        width: double.infinity,
                        child: ElevatedButton(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppTheme.primaryOrange,
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(14),
                            ),
                          ),
                          onPressed: () async {
                            await Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => const CartScreen(),
                              ),
                            );

                            // In case user cleared cart/checked out and came back
                            if (!context.mounted) return;
                            await context.read<CartProvider>().loadCart();
                          },
                          child: Row(
                            children: [
                              const Icon(Icons.shopping_cart_outlined),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Text(
                                  "View cart • ${cart.totalItems} item(s)",
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w900,
                                    fontSize: 14,
                                  ),
                                ),
                              ),
                              Text(
                                "${cart.subtotal.toStringAsFixed(2)} EGP",
                                style: const TextStyle(
                                  fontWeight: FontWeight.w900,
                                  fontSize: 14,
                                ),
                              ),
                              const SizedBox(width: 6),
                              const Icon(Icons.chevron_right),
                            ],
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

  // ---------- Data prep ----------
  void _prepareCategories(List<MenuItem> items) {
    final seen = <String>{};
    final cats = <String>["All"];

    for (final it in items) {
      final c = (it.category == null || it.category!.trim().isEmpty)
          ? "Other"
          : it.category!.trim();
      if (seen.add(c)) cats.add(c);
    }

    _categories = cats;

    for (final c in _categories) {
      if (c == "All") continue;
      _categoryHeaderKeys.putIfAbsent(c, () => GlobalKey());
    }

    if (_selectedCategoryIndex >= _categories.length) {
      _selectedCategoryIndex = 0;
    }
  }

  // ---------- Pinned categories (chips) ----------
  SliverPersistentHeader _buildPinnedCategories() {
    return SliverPersistentHeader(
      pinned: true,
      delegate: _PinnedHeaderDelegate(
        height: 58,
        child: Container(
          color: const Color(0xFFF6F6F6),
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            itemCount: _categories.length,
            itemBuilder: (context, index) {
              final isSelected = _selectedCategoryIndex == index;

              return GestureDetector(
                onTap: () => _onCategoryTap(index),
                child: Container(
                  margin: const EdgeInsets.only(right: 10),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    padding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 10,
                    ),
                    decoration: BoxDecoration(
                      color: isSelected ? AppTheme.primaryOrange : Colors.white,
                      borderRadius: BorderRadius.circular(22),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 8,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: Text(
                      _categories[index],
                      style: TextStyle(
                        color: isSelected ? Colors.white : Colors.black87,
                        fontWeight: FontWeight.w800,
                        fontSize: 13,
                      ),
                    ),
                  ),
                ),
              );
            },
          ),
        ),
      ),
    );
  }

  Future<void> _onCategoryTap(int index) async {
    setState(() => _selectedCategoryIndex = index);

    if (index == 0) {
      _programmaticScroll = true;
      await _scrollController.animateTo(
        0,
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeOut,
      );
      _programmaticScroll = false;
      return;
    }

    final category = _categories[index];
    final key = _categoryHeaderKeys[category];
    final ctx = key?.currentContext;
    if (ctx == null) return;

    _programmaticScroll = true;

    await Scrollable.ensureVisible(
      ctx,
      duration: const Duration(milliseconds: 350),
      curve: Curves.easeOut,
      alignment: 0.02,
    );

    _programmaticScroll = false;
  }

  void _onScroll() {
    if (_programmaticScroll) return;
    if (!mounted) return;

    const double pinnedBarOffset = 70;

    String? bestCategory;
    double bestDistance = double.infinity;

    for (final entry in _categoryHeaderKeys.entries) {
      final ctx = entry.value.currentContext;
      if (ctx == null) continue;

      final render = ctx.findRenderObject();
      if (render is! RenderBox) continue;
      if (!render.hasSize) continue;

      final dy = render.localToGlobal(Offset.zero).dy;
      final distance = (dy - pinnedBarOffset).abs();

      if (distance < bestDistance) {
        bestDistance = distance;
        bestCategory = entry.key;
      }
    }

    if (bestCategory == null) return;

    final newIndex = _categories.indexOf(bestCategory);
    if (newIndex != -1 && newIndex != _selectedCategoryIndex) {
      setState(() => _selectedCategoryIndex = newIndex);
    }
  }

  // ---------- Header ----------
  SliverAppBar _buildHeader() {
    final imageUrl = widget.restaurant.imageUrl;

    return SliverAppBar(
      expandedHeight: 260,
      pinned: true,
      elevation: 0,
      backgroundColor: Colors.transparent,
      iconTheme: const IconThemeData(color: Colors.white),
      flexibleSpace: FlexibleSpaceBar(
        background: Stack(
          fit: StackFit.expand,
          children: [
            (imageUrl != null && imageUrl.isNotEmpty)
                ? Image.network(
              imageUrl,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => _headerPlaceholder(),
            )
                : _headerPlaceholder(),
            Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    Colors.black.withOpacity(0.10),
                    Colors.black.withOpacity(0.78),
                  ],
                ),
              ),
            ),
            SafeArea(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 0, 20, 18),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.end,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.restaurant.name,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 24,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      widget.restaurant.cuisine,
                      style: const TextStyle(
                        color: Colors.white70,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(height: 14),
                    Row(
                      children: [
                        _infoChip(
                          Icons.star,
                          widget.restaurant.rating.toStringAsFixed(1),
                        ),
                        const SizedBox(width: 12),
                        _infoChip(Icons.timer, "30–45 min"),
                        const SizedBox(width: 12),
                        _infoChip(Icons.delivery_dining, "Delivery"),
                      ],
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

  Widget _headerPlaceholder() {
    return Container(
      color: AppTheme.primaryOrange.withOpacity(0.22),
      child: const Center(
        child: Icon(Icons.restaurant, size: 56, color: Colors.white),
      ),
    );
  }

  Widget _infoChip(IconData icon, String text) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.18),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Icon(icon, size: 16, color: Colors.white),
          const SizedBox(width: 6),
          Text(
            text,
            style: const TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  // ---------- Menu grouped by category ----------
  List<Widget> _buildMenuByCategorySlivers(List<MenuItem> items) {
    final Map<String, List<MenuItem>> grouped = {};
    for (final item in items) {
      final key = (item.category == null || item.category!.trim().isEmpty)
          ? "Other"
          : item.category!.trim();
      grouped.putIfAbsent(key, () => []);
      grouped[key]!.add(item);
    }

    final categories = grouped.keys.toList();
    final List<Widget> slivers = [];

    for (final cat in categories) {
      slivers.add(
        SliverToBoxAdapter(
          child: Container(
            key: _categoryHeaderKeys[cat],
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 10),
            child: Text(
              cat,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
        ),
      );

      slivers.add(
        SliverPadding(
          padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
          sliver: SliverList(
            delegate: SliverChildBuilderDelegate(
                  (context, index) {
                final item = grouped[cat]![index];
                return Padding(
                  padding: const EdgeInsets.only(bottom: 14),
                  child: MenuItemCard(
                    item: item,
                    onTap: () => _openMenuItemDetails(item),
                  ),
                );
              },
              childCount: grouped[cat]!.length,
            ),
          ),
        ),
      );
    }

    slivers.add(const SliverToBoxAdapter(child: SizedBox(height: 30)));
    return slivers;
  }

  // ---------- Menu item details ----------
  void _openMenuItemDetails(MenuItem item) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) {
        return DraggableScrollableSheet(
          initialChildSize: 0.72,
          minChildSize: 0.55,
          maxChildSize: 0.92,
          builder: (context, controller) {
            int qty = 1;

            return StatefulBuilder(
              builder: (context, setSheetState) {
                final total = item.price * qty;

                return Container(
                  decoration: const BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.vertical(top: Radius.circular(22)),
                  ),
                  child: ListView(
                    controller: controller,
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
                    children: [
                      Center(
                        child: Container(
                          width: 44,
                          height: 5,
                          margin: const EdgeInsets.only(bottom: 14),
                          decoration: BoxDecoration(
                            color: Colors.black12,
                            borderRadius: BorderRadius.circular(20),
                          ),
                        ),
                      ),
                      if ((item.imageUrl ?? '').isNotEmpty) ...[
                        ClipRRect(
                          borderRadius: BorderRadius.circular(16),
                          child: Image.network(
                            item.imageUrl!,
                            height: 190,
                            fit: BoxFit.cover,
                            errorBuilder: (_, __, ___) => Container(
                              height: 190,
                              color: AppTheme.primaryOrange.withOpacity(0.12),
                              child: const Center(
                                child: Icon(
                                  Icons.fastfood_rounded,
                                  color: AppTheme.primaryOrange,
                                  size: 34,
                                ),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(height: 14),
                      ],
                      Text(
                        item.name,
                        style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      if ((item.description ?? '').isNotEmpty) ...[
                        const SizedBox(height: 8),
                        Text(
                          item.description!,
                          style: TextStyle(
                            color: Colors.grey[700],
                            height: 1.25,
                          ),
                        ),
                      ],
                      const SizedBox(height: 14),
                      Row(
                        children: [
                          const Text(
                            "Quantity",
                            style: TextStyle(fontWeight: FontWeight.w800),
                          ),
                          const Spacer(),
                          IconButton(
                            onPressed: qty > 1 ? () => setSheetState(() => qty--) : null,
                            icon: const Icon(Icons.remove_circle_outline),
                          ),
                          Text(
                            qty.toString(),
                            style: const TextStyle(
                              fontWeight: FontWeight.w900,
                              fontSize: 16,
                            ),
                          ),
                          IconButton(
                            onPressed: () => setSheetState(() => qty++),
                            icon: const Icon(Icons.add_circle_outline),
                          ),
                        ],
                      ),
                      const SizedBox(height: 18),
                      SizedBox(
                        height: 52,
                        child: ElevatedButton(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppTheme.primaryOrange,
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(14),
                            ),
                          ),
                          onPressed: item.isAvailable
                              ? () async {
                            try {
                              await this.context.read<CartProvider>().addItem(
                                restaurantId: widget.restaurant.id,
                                menuItemId: item.id,
                                quantity: qty,
                                customizations: null,
                              );

                              if (!context.mounted) return;
                              Navigator.pop(context);

                              _showAppSnack(
                                this.context,
                                "Added to cart",
                                type: AppSnackType.success,
                              );
                            } catch (e) {
                              if (!context.mounted) return;

                              final msg = e.toString();

                              if (msg.toLowerCase().contains("different restaurants")) {
                                final confirm =
                                await _showClearCartDialog(this.context);
                                if (confirm == true) {
                                  try {
                                    await this.context.read<CartProvider>().clear();
                                    await this.context.read<CartProvider>().addItem(
                                      restaurantId: widget.restaurant.id,
                                      menuItemId: item.id,
                                      quantity: qty,
                                      customizations: null,
                                    );

                                    if (!context.mounted) return;
                                    Navigator.pop(context);

                                    _showAppSnack(
                                      this.context,
                                      "Cart cleared and item added",
                                      type: AppSnackType.success,
                                    );
                                  } catch (e) {
                                    if (!context.mounted) return;
                                    _showAppSnack(
                                      this.context,
                                      e.toString(),
                                      type: AppSnackType.error,
                                    );
                                  }
                                }
                                return;
                              }

                              _showAppSnack(
                                this.context,
                                msg,
                                type: AppSnackType.error,
                              );
                            }
                          }
                              : null,
                          child: Text(
                            item.isAvailable
                                ? "Add to cart • ${total.toStringAsFixed(2)} EGP"
                                : "Unavailable",
                            style: const TextStyle(
                              fontWeight: FontWeight.w900,
                              fontSize: 14,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              },
            );
          },
        );
      },
    );
  }
}

enum AppSnackType { success, error, info }

void _showAppSnack(
    BuildContext context,
    String text, {
      AppSnackType type = AppSnackType.info,
    }) {
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

Future<bool?> _showClearCartDialog(BuildContext context) {
  return showDialog<bool>(
    context: context,
    builder: (ctx) {
      return AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text("Start a new cart?"),
        content: const Text(
          "Your cart has items from another restaurant. Clear it to add items from this restaurant.",
        ),
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
            child: const Text("Clear & Add"),
          ),
        ],
      );
    },
  );
}

class _PinnedHeaderDelegate extends SliverPersistentHeaderDelegate {
  final double height;
  final Widget child;

  _PinnedHeaderDelegate({required this.height, required this.child});

  @override
  double get minExtent => height;

  @override
  double get maxExtent => height;

  @override
  Widget build(BuildContext context, double shrinkOffset, bool overlapsContent) {
    return child;
  }

  @override
  bool shouldRebuild(covariant _PinnedHeaderDelegate oldDelegate) {
    return oldDelegate.height != height || oldDelegate.child != child;
  }
}
