import 'dart:convert';
import 'package:http/http.dart' as http;

import '../constants.dart';
import 'token_service.dart';

class DriverService {
  static Future<List<dynamic>> getMyDriverDeliveries() async {
    final token = await TokenService.getToken();
    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/my-driver-deliveries"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    final body = jsonDecode(res.body);
    if (res.statusCode == 200) return (body as List);
    throw Exception(body['message'] ?? "Failed to load driver deliveries");
  }

  // Your backend returns a LIST here (not a single object)
  static Future<List<dynamic>> getActiveDeliveries() async {
    final token = await TokenService.getToken();
    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/driver/active"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 204) return [];

    final body = jsonDecode(res.body);
    if (res.statusCode == 200) return (body as List);
    if (res.statusCode == 404) return [];

    throw Exception(body['message'] ?? "Failed to load active deliveries");
  }

  static Future<void> pickupConfirm(int deliveryId) async {
    final token = await TokenService.getToken();
    final res = await http.put(
      Uri.parse("$baseUrl/deliveries/$deliveryId/pickup-confirmation"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Pickup confirmation failed");
  }

  static Future<void> deliveryConfirm(int deliveryId) async {
    final token = await TokenService.getToken();
    final res = await http.put(
      Uri.parse("$baseUrl/deliveries/$deliveryId/delivery-confirmation"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Delivery confirmation failed");
  }

  static Future<void> updateMyProfile({
    required String vehicleType,
    required String vehicleNumber,
    required String driverStatus,
  }) async {
    final token = await TokenService.getToken();

    final res = await http.put(
      Uri.parse('$baseUrl/drivers/my-profile'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'vehicleType': vehicleType,
        'vehicleNumber': vehicleNumber,
        'driverStatus': driverStatus,
      }),
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;

    final body = res.body.isNotEmpty ? jsonDecode(res.body) : null;
    throw Exception(body?['message'] ?? 'Update profile failed');
  }

  static Future<void> updateDriverStatus({
    required int driverId,
    required String status, // e.g. "BUSY"
  }) async {
    final token = await TokenService.getToken();

    final res = await http.put(
      Uri.parse('$baseUrl/drivers/$driverId/status'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'status': status}),
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;

    final body = res.body.isNotEmpty ? jsonDecode(res.body) : null;
    throw Exception(body?['message'] ?? 'Update status failed');
  }
}
