import 'dart:convert';
import 'package:http/http.dart' as http;

import '../constants.dart';
import 'token_service.dart';

class PaymentService {
  /// Create a payment for an order
  /// For CASH: Payment is created and immediately marked as pending/completed
  /// For CARD: Payment is created and may require confirmation (optional)
  static Future<Map<String, dynamic>> createPayment({
    required int orderId,
    required String paymentMethod, // "CARD" or "CASH"
    bool autoConfirm = true, // Auto-confirm after creation (default: true)
  }) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final body = {
      "orderId": orderId,
      "paymentMethod": paymentMethod,
    };

    print("💳 Creating payment: $body");

    final response = await http.post(
      Uri.parse("$baseUrl/payments"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(body),
    );

    print("💳 Payment response status: ${response.statusCode}");
    print("💳 Payment response body: '${response.body}'");

    // Accept ALL success status codes (200, 201, 204)
    if (response.statusCode >= 200 && response.statusCode < 300) {
      Map<String, dynamic> paymentResult;

      try {
        // Handle empty response body
        if (response.body.isEmpty || response.body.trim().isEmpty) {
          print("💳 Empty response body - Payment successful");
          paymentResult = {
            "success": true,
            "orderId": orderId,
            "paymentMethod": paymentMethod,
            "message": "Payment processed successfully"
          };
        } else {
          // Try to parse JSON response
          final responseBody = jsonDecode(response.body);

          if (responseBody is Map<String, dynamic>) {
            print("💳 Payment successful - Map response");
            paymentResult = {
              ...responseBody,
              "success": true,
            };
          } else {
            print("💳 Payment successful - Non-map response");
            paymentResult = {
              "data": responseBody,
              "success": true,
              "orderId": orderId,
              "paymentMethod": paymentMethod,
            };
          }
        }
      } catch (e) {
        print("💳 JSON parsing failed but status is success: $e");
        paymentResult = {
          "success": true,
          "orderId": orderId,
          "paymentMethod": paymentMethod,
          "message": "Payment processed successfully",
          "rawResponse": response.body,
        };
      }

      // IMPORTANT: Only auto-confirm if:
      // 1. autoConfirm is true
      // 2. Payment method is CARD
      // 3. Payment has an ID (paymentId)
      // 4. Status is PENDING
      if (autoConfirm &&
          paymentMethod == "CARD" &&
          paymentResult['paymentId'] != null &&
          paymentResult['status'] == 'PENDING') {

        print("💳 Auto-confirming CARD payment...");

        try {
          // Try to confirm, but don't fail if confirmation fails
          final confirmedPayment = await confirmPayment(
            paymentId: paymentResult['paymentId'] as int,
          );

          print("✅ Payment confirmed successfully");
          return confirmedPayment;

        } catch (e) {
          print("⚠️ Confirmation failed, but payment was created: $e");

          // CRITICAL: Payment was created successfully
          // Even if confirmation fails, return success
          // The backend issue needs to be fixed separately
          return {
            ...paymentResult,
            "success": true,
            "confirmationError": e.toString(),
            "message": "Payment created successfully (confirmation pending)"
          };
        }
      }

      return paymentResult;
    }

    // Only throw exception if status code indicates actual failure
    print("❌ Payment failed with status: ${response.statusCode}");
    throw Exception(_extractError(response));
  }

  /// Confirm a payment (for two-step payment flows)
  /// This is OPTIONAL - only call if needed
  static Future<Map<String, dynamic>> confirmPayment({
    required int paymentId,
  }) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    print("✅ Confirming payment: $paymentId");

    final response = await http.post(
      Uri.parse("$baseUrl/payments/$paymentId/confirm"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    print("✅ Confirm response status: ${response.statusCode}");
    print("✅ Confirm response body: '${response.body}'");

    // Accept ALL success status codes
    if (response.statusCode >= 200 && response.statusCode < 300) {
      try {
        // Handle empty response body
        if (response.body.isEmpty || response.body.trim().isEmpty) {
          print("✅ Empty response body - Confirmation successful");
          return {
            "success": true,
            "paymentId": paymentId,
            "message": "Payment confirmed successfully"
          };
        }

        final responseBody = jsonDecode(response.body);

        if (responseBody is Map<String, dynamic>) {
          print("✅ Confirmation successful - Map response");
          return {
            ...responseBody,
            "success": true,
          };
        }

        print("✅ Confirmation successful - Non-map response");
        return {
          "data": responseBody,
          "success": true,
          "paymentId": paymentId,
        };
      } catch (e) {
        print("✅ JSON parsing failed but status is success: $e");
        return {
          "success": true,
          "paymentId": paymentId,
          "message": "Payment confirmed successfully",
          "rawResponse": response.body,
        };
      }
    }

    // Only throw exception for actual failures
    print("❌ Confirmation failed with status: ${response.statusCode}");
    throw Exception(_extractError(response));
  }

  /// Create payment without auto-confirmation
  /// Use this if you want to manually control the confirmation step
  static Future<Map<String, dynamic>> createPaymentOnly({
    required int orderId,
    required String paymentMethod,
  }) async {
    return createPayment(
      orderId: orderId,
      paymentMethod: paymentMethod,
      autoConfirm: false, // Don't auto-confirm
    );
  }

  /// Get payment history for the current user
  static Future<List<dynamic>> getPaymentHistory() async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final response = await http.get(
      Uri.parse("$baseUrl/payments/my-payments"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is List) {
        return body;
      }
      if (body is Map && body.containsKey('data')) {
        return body['data'] as List;
      }
      return [];
    }

    throw Exception(_extractError(response));
  }

  /// Get a specific payment by ID
  static Future<Map<String, dynamic>> getPaymentById(int paymentId) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final response = await http.get(
      Uri.parse("$baseUrl/payments/$paymentId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    }

    throw Exception(_extractError(response));
  }

  /// Extract error message from response
  static String _extractError(http.Response response) {
    try {
      final decoded = jsonDecode(response.body);
      if (decoded is Map && decoded['message'] != null) {
        return decoded['message'].toString();
      }
      if (decoded is Map && decoded['error'] != null) {
        return decoded['error'].toString();
      }
      return response.body.toString();
    } catch (_) {
      // If response body is not JSON, return generic error
      if (response.body.isEmpty) {
        return "Payment request failed (${response.statusCode})";
      }
      return "Payment error: ${response.body}";
    }
  }
}