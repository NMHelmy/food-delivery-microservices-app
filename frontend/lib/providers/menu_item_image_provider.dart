import 'package:flutter/foundation.dart';
import '../services/restaurants_service.dart';

class MenuItemImageProvider extends ChangeNotifier {
  // key format: "restaurantId:menuItemId"
  final Map<String, String?> _cache = {};
  final Set<String> _inFlight = {};

  String? getImageUrl({
    required int restaurantId,
    required int menuItemId,
  }) {
    return _cache["$restaurantId:$menuItemId"];
  }

  bool hasEntry({
    required int restaurantId,
    required int menuItemId,
  }) {
    return _cache.containsKey("$restaurantId:$menuItemId");
  }

  Future<void> warmUp({
    required int restaurantId,
    required int menuItemId,
  }) async {
    final key = "$restaurantId:$menuItemId";

    // Already fetched (even if null)
    if (_cache.containsKey(key)) return;

    // Avoid duplicate parallel requests
    if (_inFlight.contains(key)) return;
    _inFlight.add(key);

    try {
      final data = await RestaurantsService.getMenuItemById(
        restaurantId: restaurantId,
        menuItemId: menuItemId,
      );

      _cache[key] = data['imageUrl'] as String?;
    } catch (_) {
      // Cache null so we don't keep retrying on every rebuild
      _cache[key] = null;
    } finally {
      _inFlight.remove(key);
    }

    notifyListeners();
  }

  void clearCache() {
    _cache.clear();
    _inFlight.clear();
    notifyListeners();
  }
}
