import 'package:flutter/material.dart';
import '../models/restaurant.dart';
import '../models/menu_item.dart';
import '../services/menu_service.dart';
import '../widgets/menu_item_card.dart';
import '../theme/app_theme.dart';

class RestaurantDetailsScreen extends StatefulWidget {
  final Restaurant restaurant;

  const RestaurantDetailsScreen({super.key, required this.restaurant});

  @override
  State<RestaurantDetailsScreen> createState() =>
      _RestaurantDetailsScreenState();
}

class _RestaurantDetailsScreenState extends State<RestaurantDetailsScreen> {
  late Future<List<MenuItem>> _menuFuture;

  @override
  void initState() {
    super.initState();
    _menuFuture = MenuService.getMenuItems(widget.restaurant.id);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF7F7F7),

      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.black),
        title: Text(
          widget.restaurant.name,
          style: const TextStyle(
            color: Colors.black,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),

      body: Column(
        children: [
          _buildHeader(),
          Expanded(child: _buildMenu()),
        ],
      ),
    );
  }

  // 🏪 Restaurant Info Header
  Widget _buildHeader() {
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            widget.restaurant.cuisine,
            style: const TextStyle(color: Colors.grey),
          ),
          const SizedBox(height: 6),

          Row(
            children: [
              const Icon(Icons.star, color: Colors.orange, size: 18),
              const SizedBox(width: 4),
              Text(widget.restaurant.rating.toStringAsFixed(1)),
              const SizedBox(width: 16),
              const Icon(Icons.timer, size: 18, color: Colors.grey),
              const SizedBox(width: 4),
              const Text("30–45 min"),
            ],
          ),

          const SizedBox(height: 8),
          Text(
            widget.restaurant.address,
            style: const TextStyle(color: Colors.grey),
          ),
        ],
      ),
    );
  }

  // 🍔 Menu List
  Widget _buildMenu() {
    return FutureBuilder<List<MenuItem>>(
      future: _menuFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        }

        if (snapshot.hasError) {
          return const Center(child: Text("Failed to load menu"));
        }

        final items = snapshot.data!;
        if (items.isEmpty) {
          return const Center(child: Text("No items available"));
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: items.length,
          itemBuilder: (context, index) {
            return MenuItemCard(item: items[index]);
          },
        );
      },
    );
  }
}
