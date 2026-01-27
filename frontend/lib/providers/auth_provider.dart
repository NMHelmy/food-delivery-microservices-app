import 'package:flutter/material.dart';
import '../models/auth_me_response.dart';
import '../services/auth_service.dart';
import '../services/token_service.dart';

class AuthProvider extends ChangeNotifier {
  AuthMeResponse? _me;
  bool _loading = false;

  AuthMeResponse? get me => _me;
  bool get isLoading => _loading;

  String get role => _me?.role ?? "CUSTOMER";
  bool get isLoggedIn => _me != null;

  /// Call this from main.dart safely (checks token first).
  Future<void> loadMeIfPossible() async {
    final token = await TokenService.getToken();
    if (token == null || token.isEmpty) {
      // Optional: try role cache if you want tabs immediately
      await loadRoleFromStorageOnly();
      return;
    }

    try {
      await loadMe();
    } catch (_) {
      // token invalid/expired
      await TokenService.clear();
      _me = null;
      notifyListeners();
    }
  }

  Future<void> loadMe() async {
    _loading = true;
    notifyListeners();

    try {
      _me = await AuthService.me();
      await TokenService.saveRole(_me!.role);
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> loadRoleFromStorageOnly() async {
    final role = await TokenService.getRole();
    if (role == null) return;

    // NOTE: this must match your AuthMeResponse model fields
    _me = AuthMeResponse(
      id: 0,
      role: role,
    );

    notifyListeners();
  }

  void clear() {
    _me = null;
    notifyListeners();
  }
}
