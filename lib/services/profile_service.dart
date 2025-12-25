import 'dart:convert';
import 'package:http/http.dart' as http;
import 'token_service.dart';

class ProfileService {
  static const String _baseUrl = "http://192.168.100.12:8085";

  static Future<Map<String, dynamic>> fetchProfile() async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final response = await http.get(
      Uri.parse("$_baseUrl/auth/me"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception(
        "Failed to load profile (${response.statusCode})",
      );
    }
  }
}
