import 'package:flutter/material.dart';
import '../models/restaurant.dart';
import '../theme/app_theme.dart';

class RestaurantCard extends StatelessWidget {
  final Restaurant restaurant;
  final VoidCallback onTap;

  const RestaurantCard({
    super.key,
    required this.restaurant,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.fromLTRB(15, 0, 15, 22),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(22),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.15),
              blurRadius: 24,
              offset: const Offset(0, 12),
            ),
          ],
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(22),
          child: Stack(
            children: [
              // 🖼 IMAGE (FULL, NO BOTTOM RADIUS)
              Stack(
                children: [
                  Image.network(
                    restaurant.imageUrl!,
                    height: 150,
                    width: double.infinity,
                    fit: BoxFit.cover,
                  ),
                  Container(
                    height: 190,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Colors.transparent,
                          Colors.black.withOpacity(0.25),
                        ],
                      ),
                    ),
                  ),
                ],
              ),

              // 🧾 INFO CARD (OVERLAPS IMAGE)
              Positioned(
                left: 0,
                right: 0,
                bottom: 0,
                child: Container(
                  padding: const EdgeInsets.fromLTRB(16, 10, 16, 10),
                  decoration: BoxDecoration(
                    color: Color(0xFFFAF7F4),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // NAME + DISTANCE
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            restaurant.name,
                            style: const TextStyle(
                              fontSize: 17,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ],
                      ),

                      const SizedBox(height: 2),

                      Text(
                        restaurant.cuisine,
                        style: const TextStyle(
                          color: Colors.grey,
                          fontSize: 13,
                        ),
                      ),

                      const SizedBox(height: 2),

                      // ⭐ RATING • ⏱ TIME • 🟢 OPEN
                      Row(
                        children: [
                          const Icon(Icons.star,
                              color: Colors.amber, size: 16),
                          const SizedBox(width: 4),
                          Text(
                            restaurant.rating.toStringAsFixed(1),
                            style: const TextStyle(
                              fontWeight: FontWeight.w600,
                              fontSize: 13,
                            ),
                          ),

                          const SizedBox(width: 12),
                          const Icon(Icons.timer,
                              size: 16, color: Colors.grey),
                          const SizedBox(width: 4),
                          const Text(
                            "30–45 min",
                            style: TextStyle(fontSize: 13),
                          ),

                          const Spacer(),

                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 12,
                              vertical: 6,
                            ),
                            decoration: BoxDecoration(
                              color: Colors.green.shade100,
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: const Text(
                              "Open",
                              style: TextStyle(
                                color: Colors.green,
                                fontWeight: FontWeight.w600,
                                fontSize: 12,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
