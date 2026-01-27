import 'package:flutter/material.dart';

class AppTheme {
  static const Color primaryOrange = Color(0xFFF55A2C);

  static ThemeData lightTheme = ThemeData(
    brightness: Brightness.light,
    primaryColor: primaryOrange,

    scaffoldBackgroundColor: const Color(0xFFF5F5F5),

    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.white,
      elevation: 0,
      foregroundColor: Colors.black,
    ),

    colorScheme: const ColorScheme.light(
      primary: primaryOrange,
      secondary: Color(0xFFFF8A65),
      background: Color(0xFFF5F5F5),
      surface: Colors.white,
      onPrimary: Colors.white,
      onSurface: Colors.black87,
    ),

    shadowColor: Colors.black12,
  );
}
