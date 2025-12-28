class AuthMeResponse {
  final int id;
  final String role;

  AuthMeResponse({required this.id, required this.role});

  factory AuthMeResponse.fromJson(Map<String, dynamic> json) {
    return AuthMeResponse(
      id: (json['userId'] as num).toInt(),
      role: (json['role'] ?? 'CUSTOMER').toString(),
    );
  }
}
