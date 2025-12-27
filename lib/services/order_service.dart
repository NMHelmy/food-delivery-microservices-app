import 'dart:convert';
import 'package:http/http.dart' as http;

import 'token_service.dart';
import '../constants.dart';

class OrdersService {

  static Future<List<dynamic>> fetchMyOrders() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/orders/customer"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception("Failed to load orders (${response.statusCode})");
    }
  }

  // Checkout current cart => creates order + clears cart in backend
  // POST /cart/checkout
  // Body: { deliveryAddressId: Long, specialInstructions: String? }
  static Future<Map<String, dynamic>> checkoutCart({
    required int deliveryAddressId,
    String? specialInstructions,
  }) async {
    final token = await TokenService.getToken();

    final body = {
      "deliveryAddressId": deliveryAddressId,
      "specialInstructions": specialInstructions,
    };

    final response = await http.post(
      Uri.parse("$baseUrl/cart/checkout"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(body),
    );

    if (response.statusCode == 201 || response.statusCode == 200) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } else {
      throw Exception(_extractError(response));
    }
  }

  static String _extractError(http.Response response) {
    try {
      final decoded = jsonDecode(response.body);
      if (decoded is Map && decoded['message'] != null) {
        return decoded['message'].toString();
      }
      return response.body.toString();
    } catch (_) {
      return "Request failed (${response.statusCode})";
    }
  }
}
