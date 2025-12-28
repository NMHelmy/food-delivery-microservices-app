import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/address.dart';
import 'token_service.dart';
import '../constants.dart';

class AddressService {

  static Future<List<Address>> getMyAddresses() async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final response = await http.get(
      Uri.parse("$baseUrl/addresses/my-addresses"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode != 200) {
      throw Exception("Failed to load addresses (${response.statusCode})");
    }

    final body = json.decode(response.body);

    // Handle if response is empty or null
    if (body == null) {
      return [];
    }

    // Handle if response is a map with a data field
    if (body is Map && body.containsKey('data')) {
      final List data = body['data'] as List;
      return data.map((e) => Address.fromJson(e)).toList();
    }

    // Handle if response is directly a list
    if (body is List) {
      return body.map((e) => Address.fromJson(e)).toList();
    }

    return [];
  }

  static Future<Address> createAddress({
    required String label,
    required String streetAddress,
    required String city,
    required String state,
    required String zipCode,
    String? landmark,
    bool? isDefault,
  }) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final payload = {
      "label": label,
      "streetAddress": streetAddress,
      "city": city,
      "state": state,
      "zipCode": zipCode,
      if (landmark != null) "landmark": landmark,
      if (isDefault != null) "isDefault": isDefault,
    };

    final response = await http.post(
      Uri.parse("$baseUrl/addresses"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(payload),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      return Address.fromJson(jsonDecode(response.body));
    }

    throw Exception(_extractError(response));
  }

  static Future<Address> updateAddress({
    required int addressId,
    String? label,
    String? streetAddress,
    String? city,
    String? state,
    String? zipCode,
    String? landmark,
    bool? isDefault,
  }) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final payload = <String, dynamic>{
      if (label != null) "label": label,
      if (streetAddress != null) "streetAddress": streetAddress,
      if (city != null) "city": city,
      if (state != null) "state": state,
      if (zipCode != null) "zipCode": zipCode,
      if (landmark != null) "landmark": landmark,
      if (isDefault != null) "isDefault": isDefault,
    };

    final response = await http.put(
      Uri.parse("$baseUrl/addresses/$addressId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
      body: jsonEncode(payload),
    );

    if (response.statusCode == 200) {
      return Address.fromJson(jsonDecode(response.body));
    }

    throw Exception(_extractError(response));
  }

  static Future<void> deleteAddress(int addressId) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final response = await http.delete(
      Uri.parse("$baseUrl/addresses/$addressId"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200 || response.statusCode == 204) {
      return;
    }

    throw Exception(_extractError(response));
  }

  static Future<void> setDefaultAddress(int addressId) async {
    final token = await TokenService.getToken();

    if (token == null || token.isEmpty) {
      throw Exception("Not authenticated");
    }

    final response = await http.patch(
      Uri.parse("$baseUrl/addresses/$addressId/set-default"),
      headers: {
        "Authorization": "Bearer $token",
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200 || response.statusCode == 204) {
      return;
    }

    throw Exception(_extractError(response));
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