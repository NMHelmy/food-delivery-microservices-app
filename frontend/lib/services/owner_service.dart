import 'dart:convert';
import 'package:http/http.dart' as http;

import '../constants.dart';
import 'token_service.dart';

class OwnerService {
  static Future<List<dynamic>> getMyRestaurants() async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final res = await http.get(
      Uri.parse("$baseUrl/restaurants/owner"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    // IMPORTANT: Return ALL restaurants (both active and inactive)
    if (res.statusCode == 200) {
      final body = jsonDecode(res.body);
      return (body as List);
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to load owner restaurants");
  }

  static Future<List<dynamic>> getMyRestaurantDeliveries() async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final res = await http.get(
      Uri.parse("$baseUrl/deliveries/my-restaurant-deliveries"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200) {
      final body = jsonDecode(res.body);
      return (body as List);
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to load restaurant deliveries");
  }

  static Future<void> activateRestaurant(int restaurantId) async {
    final token = await TokenService.getToken();
    final res = await http.patch(
      Uri.parse("$baseUrl/restaurants/$restaurantId/activate"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to activate restaurant");
  }

  static Future<void> deactivateRestaurant(int restaurantId) async {
    final token = await TokenService.getToken();
    final res = await http.patch(
      Uri.parse("$baseUrl/restaurants/$restaurantId/deactivate"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to deactivate restaurant");
  }

  // -------- Additional Restaurant Methods --------

  static Future<Map<String, dynamic>> getRestaurant(int restaurantId) async {
    final token = await TokenService.getToken();
    final res = await http.get(
      Uri.parse("$baseUrl/restaurants/$restaurantId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200) {
      final body = jsonDecode(res.body);
      return (body as Map).cast<String, dynamic>();
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to load restaurant");
  }

  static Future<Map<String, dynamic>> createRestaurant({
    required String name,
    required String description,
    required String cuisineType,
    required String address,
    required String city,
    required String district,
    String? phone,
    String? logoUrl,
  }) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final payload = {
      "name": name,
      "description": description,
      "cuisine": cuisineType,  // Backend expects "cuisine", not "cuisineType"
      "address": address,
      "city": city,
      "district": district,
      if (phone != null && phone.isNotEmpty) "phone": phone,
      if (logoUrl != null && logoUrl.isNotEmpty) "logoUrl": logoUrl,
    };

    print("🍽️ Creating restaurant with payload: $payload");

    final res = await http.post(
      Uri.parse("$baseUrl/restaurants"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(payload),
    );

    print("🍽️ Create restaurant response status: ${res.statusCode}");
    print("🍽️ Create restaurant response body: ${res.body}");

    // Accept both 200 and 201 as success
    if (res.statusCode == 200 || res.statusCode == 201) {
      try {
        final body = jsonDecode(res.body);
        return (body as Map).cast<String, dynamic>();
      } catch (e) {
        print("❌ Failed to parse response: $e");
        // If parsing fails but status is success, return a basic success object
        return {
          "success": true,
          "message": "Restaurant created successfully",
        };
      }
    }

    // Handle error response
    try {
      final body = jsonDecode(res.body);

      // Check for validation errors object
      if (body is Map && body['errors'] != null) {
        // Extract all validation errors
        final errors = body['errors'] as Map;
        final errorMessages = errors.entries
            .map((e) => "${e.key}: ${e.value}")
            .join(", ");
        print("❌ Validation errors: $errorMessages");
        throw Exception("Validation failed: $errorMessages");
      }

      // Check for single error message
      final errorMessage = body['message'] ?? body['error'] ?? "Failed to create restaurant";
      print("❌ Error message: $errorMessage");
      throw Exception(errorMessage);
    } catch (e) {
      if (e is Exception) rethrow;
      print("❌ Error parsing error response: $e");
      throw Exception("Failed to create restaurant (${res.statusCode}): ${res.body}");
    }
  }

  static Future<Map<String, dynamic>> updateRestaurant({
    required int restaurantId,
    String? name,
    String? description,
    String? cuisineType,
    String? address,
    String? city,
    String? district,
    String? phone,
    String? logoUrl,
    bool? isActive,
  }) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final payload = {
      if (name != null && name.isNotEmpty) "name": name,
      if (description != null && description.isNotEmpty) "description": description,
      if (cuisineType != null && cuisineType.isNotEmpty) "cuisine": cuisineType,  // Backend expects "cuisine"
      if (address != null && address.isNotEmpty) "address": address,
      if (city != null && city.isNotEmpty) "city": city,
      if (district != null && district.isNotEmpty) "district": district,
      if (phone != null) "phone": phone.isEmpty ? null : phone,
      if (logoUrl != null) "logoUrl": logoUrl.isEmpty ? null : logoUrl,
      if (isActive != null) "isActive": isActive,
    };

    print("🍽️ Updating restaurant $restaurantId with payload: $payload");

    final res = await http.put(
      Uri.parse("$baseUrl/restaurants/$restaurantId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(payload),
    );

    print("🍽️ Update restaurant response status: ${res.statusCode}");

    if (res.statusCode == 200) {
      final body = jsonDecode(res.body);
      return (body as Map).cast<String, dynamic>();
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to update restaurant");
  }

  static Future<void> deleteRestaurant(int restaurantId) async {
    final token = await TokenService.getToken();
    final res = await http.delete(
      Uri.parse("$baseUrl/restaurants/$restaurantId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;
    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to delete restaurant");
  }

  // -------- Menu Management --------

  static Future<List<dynamic>> getMenu(int restaurantId) async {
    final token = await TokenService.getToken();
    final res = await http.get(
      Uri.parse("$baseUrl/restaurants/$restaurantId/menu"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200) {
      final body = jsonDecode(res.body);
      return (body as List);
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to load menu");
  }

  static Future<Map<String, dynamic>> addMenuItem({
    required int restaurantId,
    required String name,
    required String description,
    required double price,
    required String category,
    String? imageUrl,
    bool? isAvailable,
  }) async {
    final token = await TokenService.getToken();
    final payload = {
      "name": name,
      "description": description,
      "price": price,
      "category": category,
      if (imageUrl != null && imageUrl.isNotEmpty) "imageUrl": imageUrl,
      if (isAvailable != null) "isAvailable": isAvailable,
    };

    final res = await http.post(
      Uri.parse("$baseUrl/restaurants/$restaurantId/menu"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(payload),
    );

    if (res.statusCode == 200 || res.statusCode == 201) {
      final body = jsonDecode(res.body);
      return (body as Map).cast<String, dynamic>();
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to add menu item");
  }

  static Future<Map<String, dynamic>> updateMenuItem({
    required int restaurantId,
    required int itemId,
    String? name,
    String? description,
    double? price,
    String? category,
    String? imageUrl,
    bool? isAvailable,
  }) async {
    final token = await TokenService.getToken();
    final payload = {
      if (name != null && name.isNotEmpty) "name": name,
      if (description != null && description.isNotEmpty) "description": description,
      if (price != null) "price": price,
      if (category != null && category.isNotEmpty) "category": category,
      if (imageUrl != null) "imageUrl": imageUrl.isEmpty ? null : imageUrl,
      if (isAvailable != null) "isAvailable": isAvailable,
    };

    final res = await http.put(
      Uri.parse("$baseUrl/restaurants/$restaurantId/menu/$itemId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(payload),
    );

    if (res.statusCode == 200) {
      final body = jsonDecode(res.body);
      return (body as Map).cast<String, dynamic>();
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to update menu item");
  }

  static Future<void> deleteMenuItem(int restaurantId, int itemId) async {
    final token = await TokenService.getToken();
    final res = await http.delete(
      Uri.parse("$baseUrl/restaurants/$restaurantId/menu/$itemId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;
    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to delete menu item");
  }

  static Future<void> toggleMenuItemAvailability(int restaurantId, int itemId) async {
    final token = await TokenService.getToken();
    final res = await http.patch(
      Uri.parse("$baseUrl/restaurants/$restaurantId/menu/$itemId/availability"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;
    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to toggle availability");
  }

  // -------- Order Management --------

  static Future<List<dynamic>> getRestaurantOrders(int restaurantId) async {
    final token = await TokenService.getToken();
    final res = await http.get(
      Uri.parse("$baseUrl/orders/restaurant/$restaurantId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (res.statusCode == 200) {
      final body = jsonDecode(res.body);
      return (body as List);
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to load orders");
  }

  static Future<void> updateOrderStatus(int orderId, String status) async {
    final token = await TokenService.getToken();
    final res = await http.put(
      Uri.parse("$baseUrl/orders/$orderId/status"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode({"status": status}),
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;
    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to update order status");
  }

  // -------- Delivery Methods --------

  static Future<Map<String, dynamic>> createDelivery({
    required int orderId,
    required int restaurantId,
    required int customerId,
    required int deliveryAddressId,
    String? restaurantAddress,
    String? deliveryAddress,
    String? deliveryNotes,
    String? estimatedDeliveryTime,
  }) async {
    final token = await TokenService.getToken();
    final payload = {
      "orderId": orderId,
      "restaurantId": restaurantId,
      "customerId": customerId,
      "deliveryAddressId": deliveryAddressId,
      if (restaurantAddress != null) "restaurantAddress": restaurantAddress,
      if (deliveryAddress != null) "deliveryAddress": deliveryAddress,
      if (deliveryNotes != null) "deliveryNotes": deliveryNotes,
      if (estimatedDeliveryTime != null) "estimatedDeliveryTime": estimatedDeliveryTime,
    };

    final res = await http.post(
      Uri.parse("$baseUrl/deliveries"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(payload),
    );

    if (res.statusCode == 200 || res.statusCode == 201) {
      final body = jsonDecode(res.body);
      return (body as Map).cast<String, dynamic>();
    }

    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to create delivery");
  }

  static Future<void> updateDeliveryStatus(int deliveryId, String status) async {
    final token = await TokenService.getToken();
    final res = await http.put(
      Uri.parse("$baseUrl/deliveries/$deliveryId/status"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode({"status": status}),
    );

    if (res.statusCode == 200 || res.statusCode == 204) return;
    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to update delivery status");
  }

  static Future<void> assignDriver(int deliveryId, int driverId) async {
    final token = await TokenService.getToken();
    final res = await http.post(
      Uri.parse("$baseUrl/deliveries/$deliveryId/assign-driver"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode({"driverId": driverId}),
    );

    if (res.statusCode == 200 || res.statusCode == 201 || res.statusCode == 204) return;
    final body = jsonDecode(res.body);
    throw Exception(body['message'] ?? "Failed to assign driver");
  }
}