class MenuItem {
  final int id;
  final String name;
  final double price;
  final String? description;
  final String? category;
  final bool isAvailable;

  MenuItem({
    required this.id,
    required this.name,
    required this.price,
    this.description,
    this.category,
    required this.isAvailable,
  });

  factory MenuItem.fromJson(Map<String, dynamic> json) {
    return MenuItem(
      id: json['id'],
      name: json['name'],
      price: (json['price'] as num).toDouble(),
      description: json['description'],
      category: json['category'],
      isAvailable: json['isAvailable'] ?? true,
    );
  }
}
