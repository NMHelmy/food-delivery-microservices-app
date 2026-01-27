import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/restaurant.dart';
import 'token_service.dart';
import '../constants.dart';

class RestaurantsService {

  static Future<List<Restaurant>> getAllRestaurants() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/restaurants"),
      headers: {
        "Authorization": "Bearer $token",
      },
    );

    if (response.statusCode != 200) {
      throw Exception("Failed to load restaurants");
    }

    final List data = json.decode(response.body);

    return data.map((json) => Restaurant.fromJson(json)).toList();
  }

  static Future<Map<String, dynamic>> getRestaurantById(int id) async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/restaurants/$id"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception("Failed to load restaurant (${response.statusCode})");
    }
  }

  // Expected endpoint: GET /restaurants/{restaurantId}/menu/{menuItemId}
  static Future<Map<String, dynamic>> getMenuItemById({
    required int restaurantId,
    required int menuItemId,
  }) async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/restaurants/$restaurantId/menu/$menuItemId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } else {
      throw Exception("Failed to load menu item (${response.statusCode})");
    }
  }
}
