import 'package:flutter/material.dart';

class AppTab {
  final String label;
  final IconData icon;
  final Widget page;

  AppTab({
    required this.label,
    required this.icon,
    required this.page,
  });
}
