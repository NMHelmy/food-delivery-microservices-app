import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenService {
  static const _storage = FlutterSecureStorage();

  static const _tokenKey = 'auth_token';
  static const _userIdKey = 'user_id';
  static const _roleKey = 'user_role';

  /// Save token & userId (existing)
  static Future<void> saveAuthData({
    required String token,
    required int userId,
  }) async {
    await _storage.write(key: _tokenKey, value: token);
    await _storage.write(key: _userIdKey, value: userId.toString());
  }

  /// Save role
  static Future<void> saveRole(String role) async {
    await _storage.write(key: _roleKey, value: role);
  }

  /// Get token
  static Future<String?> getToken() async {
    return await _storage.read(key: _tokenKey);
  }

  /// Get userId
  static Future<int?> getUserId() async {
    final value = await _storage.read(key: _userIdKey);
    return value != null ? int.tryParse(value) : null;
  }

  /// Get role
  static Future<String?> getRole() async {
    return await _storage.read(key: _roleKey);
  }

  /// Clear all auth data (logout)
  static Future<void> clear() async {
    await _storage.delete(key: _tokenKey);
    await _storage.delete(key: _userIdKey);
    await _storage.delete(key: _roleKey);
  }
}
