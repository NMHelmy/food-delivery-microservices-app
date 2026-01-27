import 'cart_item.dart';

class Cart {
  final int id;
  final int customerId;
  final int restaurantId;
  final String restaurantName;
  final List<CartItem> items;
  final double subtotal;
  final int totalItems;
  final String? createdAt;
  final String? updatedAt;
  final String? expiresAt;

  Cart({
    required this.id,
    required this.customerId,
    required this.restaurantId,
    required this.restaurantName,
    required this.items,
    required this.subtotal,
    required this.totalItems,
    this.createdAt,
    this.updatedAt,
    this.expiresAt,
  });

  factory Cart.fromJson(Map<String, dynamic> json) {
    final itemsJson = (json['items'] as List?) ?? [];
    return Cart(
      id: json['id'],
      customerId: json['customerId'],
      restaurantId: json['restaurantId'],
      restaurantName: json['restaurantName'] ?? "Unknown Restaurant",
      items: itemsJson.map((e) => CartItem.fromJson(e)).toList().cast<CartItem>(),
      subtotal: (json['subtotal'] as num).toDouble(),
      totalItems: json['totalItems'] ?? 0,
      createdAt: json['createdAt'],
      updatedAt: json['updatedAt'],
      expiresAt: json['expiresAt'],
    );
  }
}
