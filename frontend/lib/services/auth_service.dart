import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/auth_response.dart';
import 'token_service.dart';
import '../constants.dart';
import '../models/auth_me_response.dart';

class AuthService {

  static Future<AuthResponse> login({
    required String email,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse("$baseUrl/auth/login"),
      headers: {"Content-Type": "application/json"},
      body: jsonEncode({
        "email": email,
        "password": password,
      }),
    );

    final body = jsonDecode(response.body);

    if (response.statusCode == 200) {
      final auth = AuthResponse.fromJson(body);

      await TokenService.saveAuthData(
        token: auth.token,
        userId: auth.userId,
      );

      await TokenService.saveRole(auth.role);

      return auth;
    } else {
      throw Exception(body['message'] ?? "Login failed");
    }
  }

  static Future<AuthResponse> register({
    required String fullName,
    required String email,
    required String phoneNumber,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse("$baseUrl/auth/register"),
      headers: {"Content-Type": "application/json"},
      body: jsonEncode({
        "email": email,
        "password": password,
        "fullName": fullName,
        "phoneNumber": phoneNumber,
        "role": "CUSTOMER",
      }),
    );

    final body = jsonDecode(response.body);

    if (response.statusCode == 200 || response.statusCode == 201) {
      final auth = AuthResponse.fromJson(body);

      await TokenService.saveAuthData(
        token: auth.token,
        userId: auth.userId,
      );

      return auth;
    } else {
      throw Exception(body['message'] ?? "Registration failed");
    }
  }

  static Future<AuthMeResponse> me() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/auth/me"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    final body = jsonDecode(response.body);

    if (response.statusCode == 200) {
      return AuthMeResponse.fromJson(body as Map<String, dynamic>);
    } else {
      throw Exception(body['message'] ?? "Failed to load user");
    }
  }

}
