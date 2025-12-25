import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/restaurant.dart';
import 'token_service.dart';

class RestaurantsService {
  static const String _baseUrl = "http://192.168.100.12:8085";

  static Future<List<Restaurant>> getAllRestaurants() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$_baseUrl/restaurants"),
      headers: {
        "Authorization": "Bearer $token",
      },
    );

    if (response.statusCode != 200) {
      throw Exception("Failed to load restaurants");
    }

    final List data = json.decode(response.body);

    return data
        .map((json) => Restaurant.fromJson(json))
        .toList();
  }

  static Future<Map<String, dynamic>> getRestaurantById(int id) async {
    final response = await http.get(
      Uri.parse("$_baseUrl/restaurants/$id"),
      headers: {
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception(
        "Failed to load restaurant (${response.statusCode})",
      );
    }
  }
}
