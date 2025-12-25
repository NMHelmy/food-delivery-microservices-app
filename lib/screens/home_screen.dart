import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/restaurants_service.dart';
import 'restaurant_details_screen.dart';
import '../models/restaurant.dart';
import '../widgets/restaurant_card.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late Future<List<Restaurant>> _restaurantsFuture;

  @override
  void initState() {
    super.initState();
    _restaurantsFuture = RestaurantsService.getAllRestaurants();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF7F7F7),

      appBar: AppBar(
        elevation: 0,
        backgroundColor: Colors.white,
        title: const Text(
          "FoodDash",
          style: TextStyle(
            color: Colors.black,
            fontWeight: FontWeight.w700,
          ),
        ),
        centerTitle: false,
      ),

      body: Column(
        children: [
          _buildSearchBar(),
          Expanded(child: _buildRestaurantList()),
        ],
      ),
    );
  }

  // 🔍 Search Bar (UI only for now)
  Widget _buildSearchBar() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
      child: Container(
        height: 50,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(14),
        ),
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: Row(
          children: const [
            Icon(Icons.search, color: Colors.grey),
            SizedBox(width: 10),
            Text(
              "Search restaurants or dishes",
              style: TextStyle(color: Colors.grey),
            ),
          ],
        ),
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

        final restaurants = snapshot.data!;
        if (restaurants.isEmpty) {
          return const Center(child: Text("No restaurants available"));
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
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
