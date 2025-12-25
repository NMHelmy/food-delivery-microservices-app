import 'dart:convert';
import 'package:http/http.dart' as http;
import 'token_service.dart';

class OrdersService {
  static const String _baseUrl = "http://192.168.100.12:8085";

  static Future<List<dynamic>> fetchMyOrders() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$_baseUrl/orders/customer"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception(
        "Failed to load orders (${response.statusCode})",
      );
    }
  }
}
