import 'dart:convert';
import 'package:http/http.dart' as http;
import 'token_service.dart';
import '../constants.dart';
import '../models/menu_item.dart';

class MenuService {

  static Future<List<MenuItem>> getMenuItems(int restaurantId) async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/restaurants/$restaurantId/menu"),
      headers: {
        "Authorization": "Bearer $token",
      },
    );

    if (response.statusCode != 200) {
      throw Exception("Failed to load menu");
    }

    final List data = json.decode(response.body);
    return data.map((e) => MenuItem.fromJson(e)).toList();
  }
}
