class AppUser {
  final int id;
  final String role; // "CUSTOMER" | "ADMIN" | "RESTAURANT_OWNER" | "DELIVERY_DRIVER"

  AppUser({
    required this.id,
    required this.role,
  });

  factory AppUser.fromJson(Map<String, dynamic> json) {
    return AppUser(
      id: (json['userId'] as num).toInt(),
      role: (json['role'] ?? 'CUSTOMER').toString(),
    );
  }
}
