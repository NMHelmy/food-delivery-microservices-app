import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/address.dart';
import 'token_service.dart';
import '../constants.dart';

class AddressService {

  static Future<List<Address>> getMyAddresses() async {
    final token = await TokenService.getToken();

    final response = await http.get(
      Uri.parse("$baseUrl/addresses/my-addresses"),
      headers: {
        "Authorization": "Bearer $token",
      },
    );

    if (response.statusCode != 200) {
      throw Exception("Failed to load addresses (${response.statusCode})");
    }

    final List data = json.decode(response.body);
    return data.map((e) => Address.fromJson(e)).toList().cast<Address>();
  }
}
