class Address {
  final int id;
  final int userId;

  final String label;
  final String streetAddress;
  final String city;
  final String state;
  final String zipCode;
  final String? landmark;

  final bool isDefault;

  final String? createdAt;
  final String? updatedAt;

  Address({
    required this.id,
    required this.userId,
    required this.label,
    required this.streetAddress,
    required this.city,
    required this.state,
    required this.zipCode,
    this.landmark,
    required this.isDefault,
    this.createdAt,
    this.updatedAt,
  });

  factory Address.fromJson(Map<String, dynamic> json) {
    return Address(
      id: json['id'],
      userId: json['userId'],
      label: json['label'] ?? '',
      streetAddress: json['streetAddress'] ?? '',
      city: json['city'] ?? '',
      state: json['state'] ?? '',
      zipCode: json['zipCode'] ?? '',
      landmark: json['landmark'],
      isDefault: json['isDefault'] == true,
      createdAt: json['createdAt']?.toString(),
      updatedAt: json['updatedAt']?.toString(),
    );
  }

  String displayTitle() => label.trim().isEmpty ? "Address #$id" : label.trim();

  String displayDetails() {
    final parts = <String>[
      streetAddress,
      city,
      state,
      zipCode,
    ].where((e) => e.trim().isNotEmpty).toList();

    final base = parts.join(", ");
    if ((landmark ?? "").trim().isEmpty) return base;
    return "$base • Landmark: ${landmark!.trim()}";
  }
}
