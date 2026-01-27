class MenuItem {
  final int id;
  final String name;
  final double price;
  final String? description;
  final String? category;
  final String? imageUrl;
  final bool isAvailable;

  MenuItem({
    required this.id,
    required this.name,
    required this.price,
    this.description,
    this.category,
    this.imageUrl,
    required this.isAvailable,
  });

  factory MenuItem.fromJson(Map<String, dynamic> json) {
    return MenuItem(
      id: json['id'],
      name: json['name'],
      price: (json['price'] as num).toDouble(),
      description: json['description'],
      category: json['category'],
      imageUrl: json['imageUrl'],
      isAvailable: (json['isAvailable'] ?? json['available'] ?? true) as bool,
    );
  }
}
