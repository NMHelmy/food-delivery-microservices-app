import 'dart:convert';
import 'package:http/http.dart' as http;

import '../constants.dart';
import '../services/token_service.dart';

class AdminService {
  static Future<Map<String, String>> _headers() async {
    final token = await TokenService.getToken();
    print("DEBUG: Token = ${token?.substring(0, 20)}..."); // Debug
    return {
      "Authorization": "Bearer $token",
      "Content-Type": "application/json",
    };
  }

  static dynamic _decode(http.Response res) {
    if (res.body.isEmpty) return null;
    return jsonDecode(res.body);
  }

  // -------- Users --------
  static Future<List<dynamic>> getUsers() async {
    final res = await http.get(
      Uri.parse("$baseUrl/auth/users"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load users");
  }

  static Future<Map<String, dynamic>> getUser(int userId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/auth/user/$userId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as Map).cast<String, dynamic>();
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load user");
  }

  static Future<List<dynamic>> getUserAddresses(int userId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/addresses/user/$userId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load user addresses");
  }

  // -------- Orders --------
  static Future<List<dynamic>> getOrders() async {
    final url = "$baseUrl/orders";
    print("DEBUG: Fetching orders from: $url"); // Debug

    final headers = await _headers();
    print("DEBUG: Headers = $headers"); // Debug

    try {
      final res = await http.get(
        Uri.parse(url),
        headers: headers,
      );

      print("DEBUG: Response status = ${res.statusCode}"); // Debug
      print("DEBUG: Response body length = ${res.body.length}"); // Debug
      print("DEBUG: Response body preview = ${res.body.substring(0, res.body.length > 200 ? 200 : res.body.length)}..."); // Debug

      final body = _decode(res);

      if (res.statusCode == 200) {
        if (body is List) {
          print("DEBUG: Successfully parsed ${body.length} orders"); // Debug
          return body;
        } else {
          print("DEBUG: ERROR - Body is not a List, it's ${body.runtimeType}"); // Debug
          throw Exception("Expected List but got ${body.runtimeType}");
        }
      }

      print("DEBUG: ERROR - Status ${res.statusCode}"); // Debug
      throw Exception((body is Map ? body['message'] : null) ?? "Failed to load orders (status ${res.statusCode})");

    } catch (e) {
      print("DEBUG: Exception caught: $e"); // Debug
      rethrow;
    }
  }

  static Future<List<dynamic>> getOrdersByCustomer(int customerId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/orders/customer/$customerId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load customer orders");
  }

  static Future<List<dynamic>> getOrdersByStatus(String status) async {
    final res = await http.get(
      Uri.parse("$baseUrl/orders/status/$status"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load orders by status");
  }

  static Future<Map<String, dynamic>> getOrder(int orderId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/orders/$orderId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as Map).cast<String, dynamic>();
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load order");
  }

  static Future<void> updateOrderStatus(int orderId, String status) async {
    final res = await http.put(
      Uri.parse("$baseUrl/orders/$orderId/status"),
      headers: await _headers(),
      body: jsonEncode({"status": status}),
    );
    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 204) return;
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to update order status");
  }

  static Future<void> updateOrderPayment(int orderId, String paymentStatus) async {
    final res = await http.put(
      Uri.parse("$baseUrl/orders/$orderId/payment"),
      headers: await _headers(),
      body: jsonEncode({"paymentStatus": paymentStatus}),
    );
    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 204) return;
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to update payment status");
  }

  // -------- Restaurants --------
  static Future<List<dynamic>> getRestaurantsByOwner(int ownerId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/restaurants/owner/$ownerId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load restaurants by owner");
  }

  // -------- Deliveries --------
  static Future<List<dynamic>> getAllDeliveries() async {
    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/admin/all"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load deliveries");
  }

  static Future<Map<String, dynamic>> getDelivery(int deliveryId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/$deliveryId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as Map).cast<String, dynamic>();
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load delivery");
  }

  static Future<List<dynamic>> getDeliveriesByCustomer(int customerId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/customer/$customerId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load customer deliveries");
  }

  static Future<List<dynamic>> getDeliveriesByDriver(int driverId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/admin/driver/$driverId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load driver deliveries");
  }

  static Future<List<dynamic>> getDeliveriesByRestaurant(int restaurantId) async {
    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/admin/restaurant/$restaurantId"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load restaurant deliveries");
  }

  static Future<Map<String, dynamic>> createDelivery({
    required int orderId,
    required int restaurantId,
    required int customerId,
    required int deliveryAddressId,
    String? deliveryNotes,
    String? estimatedDeliveryTime,
  }) async {
    final payload = {
      "orderId": orderId,
      "restaurantId": restaurantId,
      "customerId": customerId,
      "deliveryAddressId": deliveryAddressId,
      if (deliveryNotes != null) "deliveryNotes": deliveryNotes,
      if (estimatedDeliveryTime != null) "estimatedDeliveryTime": estimatedDeliveryTime,
    };

    final res = await http.post(
      Uri.parse("$baseUrl/deliveries"),
      headers: await _headers(),
      body: jsonEncode(payload),
    );

    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 201) {
      return (body as Map).cast<String, dynamic>();
    }
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to create delivery");
  }

  static Future<void> updateDeliveryStatus(int deliveryId, String status) async {
    final res = await http.put(
      Uri.parse("$baseUrl/deliveries/$deliveryId/status"),
      headers: await _headers(),
      body: jsonEncode({"status": status}),
    );
    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 204) return;
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to update delivery status");
  }

  static Future<void> assignDriver(int deliveryId, int driverId) async {
    final res = await http.post(
      Uri.parse("$baseUrl/deliveries/$deliveryId/assign-driver"),
      headers: await _headers(),
      body: jsonEncode({"driverId": driverId}),
    );
    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 201 || res.statusCode == 204) return;
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to assign driver");
  }

  // -------- Drivers --------
  static Future<List<dynamic>> getDriversAvailable() async {
    final res = await http.get(
      Uri.parse("$baseUrl/drivers/available"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load available drivers");
  }
  static Future<void> activateDriver(int driverId) async {
    final res = await http.put(
      Uri.parse('$baseUrl/drivers/$driverId/activate'),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 204) return;
    throw Exception(body is Map ? body['message'] ?? 'Failed to activate driver' : 'Failed to activate driver');
  }

  static Future<void> deactivateDriver(int driverId) async {
    final res = await http.put(
      Uri.parse('$baseUrl/drivers/$driverId/deactivate'),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 204) return;
    throw Exception(body is Map ? body['message'] ?? 'Failed to deactivate driver' : 'Failed to deactivate driver');
  }

  // -------- Notifications --------
  static Future<void> sendNotification({
    required String title,
    required String message,
    int? userId,
    String? role,
  }) async {
    final payload = {
      "title": title,
      "message": message,
      if (userId != null) "userId": userId,
      if (role != null) "role": role,
    };

    final res = await http.post(
      Uri.parse("$baseUrl/notifications/send"),
      headers: await _headers(),
      body: jsonEncode(payload),
    );

    final body = _decode(res);
    if (res.statusCode == 200 || res.statusCode == 201 || res.statusCode == 204) return;
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to send notification");
  }

  // Get all drivers (already added earlier)
  static Future<List<dynamic>> getAllDrivers() async {
    final res = await http.get(
      Uri.parse("$baseUrl/drivers"),
      headers: await _headers(),
    );
    final body = _decode(res);
    if (res.statusCode == 200) return (body as List);
    throw Exception((body is Map ? body['message'] : null) ?? "Failed to load drivers");
  }
}