import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/cart.dart';
import 'token_service.dart';

class CartService {
  static const String baseUrl = "http://192.168.100.12:8085";

  static Future<Cart?> getCart() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/cart"),
      headers: {
        "Authorization": "Bearer $token",
      },
    );

    // Treat empty cart as normal
    if (response.statusCode == 404) return null;

    if (response.statusCode != 200) {
      throw Exception("Failed to load cart (${response.statusCode})");
    }

    return Cart.fromJson(json.decode(response.body));
  }

  static Future<Cart> addItem({
    required int restaurantId,
    required int menuItemId,
    required int quantity,
    String? customizations,
  }) async {
    final token = await TokenService.getToken();

    final body = {
      "restaurantId": restaurantId,
      "menuItemId": menuItemId,
      "quantity": quantity,
      "customizations": customizations,
    };

    final response = await http.post(
      Uri.parse("$baseUrl/cart/items"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: json.encode(body),
    );

    if (response.statusCode != 201 && response.statusCode != 200) {
      throw Exception(_extractError(response));
    }

    return Cart.fromJson(json.decode(response.body));
  }

  static Future<Cart?> updateItem({
    required int cartItemId,
    required int quantity,
    String? customizations,
  }) async {
    final token = await TokenService.getToken();

    final body = {
      "quantity": quantity,
      "customizations": customizations,
    };

    final response = await http.put(
      Uri.parse("$baseUrl/cart/items/$cartItemId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: json.encode(body),
    );

    if (response.statusCode == 404) return null;
    if (response.statusCode != 200) {
      throw Exception(_extractError(response));
    }

    return Cart.fromJson(json.decode(response.body));
  }

  static Future<Cart?> removeItem(int cartItemId) async {
    final token = await TokenService.getToken();

    final response = await http.delete(
      Uri.parse("$baseUrl/cart/items/$cartItemId"),
      headers: {
        "Authorization": "Bearer $token",
      },
    );

    // Backend may return 404 when cart becomes empty; treat as empty cart
    if (response.statusCode == 404) return null;

    if (response.statusCode != 200) {
      throw Exception(_extractError(response));
    }

    return Cart.fromJson(json.decode(response.body));
  }

  static Future<void> clearCart() async {
    final token = await TokenService.getToken();

    final response = await http.delete(
      Uri.parse("$baseUrl/cart"),
      headers: {
        "Authorization": "Bearer $token",
      },
    );

    if (response.statusCode == 204) return;
    if (response.statusCode == 404) return; // already empty
    throw Exception("Failed to clear cart (${response.statusCode})");
  }

  static String _extractError(http.Response response) {
    try {
      final decoded = json.decode(response.body);
      if (decoded is Map && decoded['message'] != null) {
        return decoded['message'].toString();
      }
      return response.body.toString();
    } catch (_) {
      return "Request failed (${response.statusCode})";
    }
  }
}
