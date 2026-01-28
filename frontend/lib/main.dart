import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'theme/app_theme.dart';
import 'screens/auth_gate.dart';
import 'providers/cart_provider.dart';
import 'providers/menu_item_image_provider.dart';
import 'providers/auth_provider.dart';
import 'providers/notification_provider.dart';  // ← ADD THIS LINE

void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()..loadMeIfPossible()),
        ChangeNotifierProvider(create: (_) => CartProvider()..loadCart()),
        ChangeNotifierProvider(create: (_) => MenuItemImageProvider()),
        ChangeNotifierProvider(create: (_) => NotificationProvider()),  // ← ADD THIS LINE
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: const AuthGate(),
    );
  }
}
