import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../theme/app_theme.dart';
import '../services/restaurants_service.dart';
import 'restaurant_details_screen.dart';
import '../models/restaurant.dart';
import '../widgets/restaurant_card.dart';
import '../providers/cart_provider.dart';
import 'cart_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late Future<List<Restaurant>> _restaurantsFuture;
  int _selectedCategoryIndex = 0;

  List<Restaurant> _allRestaurants = [];
  List<Restaurant> _filteredRestaurants = [];
  String _searchQuery = "";

  final List<String> _categories = [
    "All",
    "Italian",
    "Asian",
    "Fast Food",
    "Desserts",
    "Egyptian",
    "Mediterranean",
    "Cafe"
  ];

  @override
  void initState() {
    super.initState();
    _restaurantsFuture = RestaurantsService.getAllRestaurants().then((data) {
      _allRestaurants = data;
      _filteredRestaurants = data;
      return data;
    });
  }

  void _applyFilters() {
    final selectedCategory = _categories[_selectedCategoryIndex];

    setState(() {
      _filteredRestaurants = _allRestaurants.where((restaurant) {
        final matchesCategory = selectedCategory == "All" ||
            restaurant.cuisine.toLowerCase() == selectedCategory.toLowerCase();

        final matchesSearch =
        restaurant.name.toLowerCase().contains(_searchQuery.toLowerCase());

        return matchesCategory && matchesSearch;
      }).toList();
    });
  }

  void _openCart() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const CartScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();

    return Scaffold(
      backgroundColor: const Color(0xFFF1F0F0),
      body: SafeArea(
        child: Column(
          children: [
            const SizedBox(height: 10),

            // Top row: title + cart badge
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                children: [
                  const Text(
                    "FoodDash",
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w900,
                      color: Colors.black,
                    ),
                  ),
                  const Spacer(),
                  _CartIconButton(
                    count: cart.totalItems,
                    onTap: _openCart,
                  ),
                ],
              ),
            ),

            _buildSearchBar(),
            _buildCategoryChips(),
            const SizedBox(height: 10),

            Expanded(child: _buildRestaurantList()),

            // Optional: Talabat-like bottom "View cart" bar
            if (cart.totalItems > 0) _ViewCartBar(onTap: _openCart),
          ],
        ),
      ),
    );
  }

  // 🔍 Search Bar
  Widget _buildSearchBar() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
      child: Container(
        height: 52,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(30),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.06),
              blurRadius: 12,
            ),
          ],
        ),
        padding: const EdgeInsets.symmetric(horizontal: 18),
        child: TextField(
          onChanged: (value) {
            setState(() {
              _searchQuery = value;
              _applyFilters();
            });
          },
          decoration: const InputDecoration(
            icon: Icon(Icons.search, color: Colors.grey),
            hintText: "Search restaurants",
            border: InputBorder.none,
          ),
        ),
      ),
    );
  }

  // 🍽 Category pills
  Widget _buildCategoryChips() {
    return SizedBox(
      height: 44,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 20),
        itemCount: _categories.length,
        itemBuilder: (context, index) {
          final bool isSelected = _selectedCategoryIndex == index;

          return GestureDetector(
            onTap: () {
              setState(() {
                _selectedCategoryIndex = index;
                _applyFilters();
              });
            },
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 220),
              curve: Curves.easeOut,
              margin: const EdgeInsets.only(right: 10),
              padding: const EdgeInsets.symmetric(
                horizontal: 14,
                vertical: 8,
              ),
              decoration: BoxDecoration(
                color: isSelected ? AppTheme.primaryOrange : Colors.white,
                borderRadius: BorderRadius.circular(22),
                boxShadow: isSelected
                    ? [
                  BoxShadow(
                    color: AppTheme.primaryOrange.withOpacity(0.35),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  ),
                ]
                    : [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.05),
                    blurRadius: 6,
                  ),
                ],
              ),
              child: Text(
                _categories[index],
                style: TextStyle(
                  color: isSelected ? Colors.white : Colors.black,
                  fontWeight: FontWeight.w600,
                  fontSize: 14,
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  // 🍽 Restaurants List
  Widget _buildRestaurantList() {
    return FutureBuilder<List<Restaurant>>(
      future: _restaurantsFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        }

        if (snapshot.hasError) {
          return const Center(child: Text("Failed to load restaurants"));
        }

        final restaurants = _filteredRestaurants;
        if (restaurants.isEmpty) {
          return const Center(child: Text("No restaurants available"));
        }

        return ListView.builder(
          padding: const EdgeInsets.only(top: 8, bottom: 16),
          itemCount: restaurants.length,
          itemBuilder: (context, index) {
            return RestaurantCard(
              restaurant: restaurants[index],
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => RestaurantDetailsScreen(
                      restaurant: restaurants[index],
                    ),
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

// ---------- UI Helpers ----------

class _CartIconButton extends StatelessWidget {
  final int count;
  final VoidCallback onTap;

  const _CartIconButton({
    required this.count,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      clipBehavior: Clip.none,
      children: [
        IconButton(
          onPressed: onTap,
          icon: const Icon(Icons.shopping_cart_outlined),
        ),
        if (count > 0)
          Positioned(
            right: 6,
            top: 6,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              decoration: BoxDecoration(
                color: AppTheme.primaryOrange,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                count.toString(),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 11,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class _ViewCartBar extends StatelessWidget {
  final VoidCallback onTap;

  const _ViewCartBar({required this.onTap});

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();

    return SafeArea(
      top: false,
      child: Container(
        margin: const EdgeInsets.fromLTRB(16, 0, 16, 12),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppTheme.primaryOrange,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: AppTheme.primaryOrange.withOpacity(0.35),
              blurRadius: 16,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: InkWell(
          onTap: onTap,
          child: Row(
            children: [
              const Icon(Icons.shopping_bag_outlined, color: Colors.white),
              const SizedBox(width: 10),
              const Text(
                "View cart",
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w900,
                  fontSize: 15,
                ),
              ),
              const Spacer(),
              Text(
                "${cart.subtotal.toStringAsFixed(2)} EGP",
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w900,
                  fontSize: 15,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// Temporary placeholder until you implement CartScreen
class _CartPlaceholderScreen extends StatelessWidget {
  const _CartPlaceholderScreen();

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text("Cart"),
      ),
      body: Center(
        child: Text(
          cart.totalItems == 0
              ? "Your cart is empty"
              : "Cart items: ${cart.totalItems}\nSubtotal: ${cart.subtotal.toStringAsFixed(2)} EGP",
          textAlign: TextAlign.center,
        ),
      ),
    );
  }
}
