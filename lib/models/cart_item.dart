class CartItem {
  final int id;
  final int menuItemId;
  final String itemName;
  final int quantity;
  final double price;
  final double itemTotal;
  final String? customizations;

  CartItem({
    required this.id,
    required this.menuItemId,
    required this.itemName,
    required this.quantity,
    required this.price,
    required this.itemTotal,
    this.customizations,
  });

  factory CartItem.fromJson(Map<String, dynamic> json) {
    return CartItem(
      id: json['id'],
      menuItemId: json['menuItemId'],
      itemName: json['itemName'],
      quantity: json['quantity'],
      price: (json['price'] as num).toDouble(),
      itemTotal: (json['itemTotal'] as num).toDouble(),
      customizations: json['customizations'],
    );
  }
}
