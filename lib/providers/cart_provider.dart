import 'package:flutter/foundation.dart';
import '../models/cart.dart';
import '../services/cart_service.dart';

class CartProvider extends ChangeNotifier {
  Cart? _cart;
  bool _loading = false;
  String? _error;

  Cart? get cart => _cart;
  bool get loading => _loading;
  String? get error => _error;

  int get totalItems => _cart?.totalItems ?? 0;
  double get subtotal => _cart?.subtotal ?? 0;

  Future<void> loadCart() async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _cart = await CartService.getCart();
    } catch (e) {
      _error = e.toString();
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> addItem({
    required int restaurantId,
    required int menuItemId,
    required int quantity,
    String? customizations,
  }) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _cart = await CartService.addItem(
        restaurantId: restaurantId,
        menuItemId: menuItemId,
        quantity: quantity,
        customizations: customizations,
      );
    } catch (e) {
      _error = e.toString();
      rethrow;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> updateItem({
    required int cartItemId,
    required int quantity,
    String? customizations,
  }) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _cart = await CartService.updateItem(
        cartItemId: cartItemId,
        quantity: quantity,
        customizations: customizations,
      );
    } catch (e) {
      _error = e.toString();
      rethrow;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> removeItem(int cartItemId) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _cart = await CartService.removeItem(cartItemId);
    } catch (e) {
      _error = e.toString();
      rethrow;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> clear() async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      await CartService.clearCart();
      _cart = null;
    } catch (e) {
      _error = e.toString();
      rethrow;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  void clearLocal() {
    _cart = null;
    _error = null;
    _loading = false;
    notifyListeners();
  }

}
