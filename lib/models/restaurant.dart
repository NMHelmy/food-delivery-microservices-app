class Restaurant {
  final int id;
  final String name;
  final String address;
  final String phone;
  final String cuisine;
  final double rating;
  final bool isActive;
  final int ownerId;
  final String description;
  final String? imageUrl;

  Restaurant({
    required this.id,
    required this.name,
    required this.address,
    required this.phone,
    required this.cuisine,
    required this.rating,
    required this.isActive,
    required this.ownerId,
    required this.description,
    this.imageUrl,
  });

  factory Restaurant.fromJson(Map<String, dynamic> json) {
    print("IMAGE URL: ${json['imageUrl']}");
    return Restaurant(
      id: json['id'],
      name: json['name'],
      address: json['address'],
      phone: json['phone'],
      cuisine: json['cuisine'],
      rating: (json['rating'] as num).toDouble(),
      isActive: json['isActive'],
      ownerId: json['ownerId'],
      description: json['description'],
      imageUrl: json['imageUrl'],
    );
  }
}
