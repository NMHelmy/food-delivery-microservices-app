class OrderItem {
  final int id;
  final int menuItemId;
  final String itemName;
  final int quantity;
  final double price;
  final String? customizations;

  OrderItem({
    required this.id,
    required this.menuItemId,
    required this.itemName,
    required this.quantity,
    required this.price,
    this.customizations,
  });

  factory OrderItem.fromJson(Map<String, dynamic> json) {
    return OrderItem(
      id: json['id'],
      menuItemId: json['menuItemId'],
      itemName: json['itemName'],
      quantity: json['quantity'],
      price: (json['price'] as num).toDouble(),
      customizations: json['customizations'],
    );
  }
}

class Order {
  final int id;
  final int customerId;
  final int restaurantId;
  final int deliveryAddressId;
  final List<OrderItem> items;

  final double subtotal;
  final double deliveryFee;
  final double tax;
  final double total;

  final String status;
  final String paymentStatus;
  final String? specialInstructions;

  final String? estimatedDeliveryTime;
  final String? actualDeliveryTime;
  final String? createdAt;
  final String? updatedAt;

  Order({
    required this.id,
    required this.customerId,
    required this.restaurantId,
    required this.deliveryAddressId,
    required this.items,
    required this.subtotal,
    required this.deliveryFee,
    required this.tax,
    required this.total,
    required this.status,
    required this.paymentStatus,
    this.specialInstructions,
    this.estimatedDeliveryTime,
    this.actualDeliveryTime,
    this.createdAt,
    this.updatedAt,
  });

  factory Order.fromJson(Map<String, dynamic> json) {
    final itemsJson = (json['items'] as List?) ?? [];
    return Order(
      id: json['id'],
      customerId: json['customerId'],
      restaurantId: json['restaurantId'],
      deliveryAddressId: json['deliveryAddressId'],
      items: itemsJson.map((e) => OrderItem.fromJson(e)).toList().cast<OrderItem>(),
      subtotal: (json['subtotal'] as num).toDouble(),
      deliveryFee: (json['deliveryFee'] as num).toDouble(),
      tax: (json['tax'] as num).toDouble(),
      total: (json['total'] as num).toDouble(),
      status: json['status']?.toString() ?? "UNKNOWN",
      paymentStatus: json['paymentStatus']?.toString() ?? "UNKNOWN",
      specialInstructions: json['specialInstructions'],
      estimatedDeliveryTime: json['estimatedDeliveryTime']?.toString(),
      actualDeliveryTime: json['actualDeliveryTime']?.toString(),
      createdAt: json['createdAt']?.toString(),
      updatedAt: json['updatedAt']?.toString(),
    );
  }
}
