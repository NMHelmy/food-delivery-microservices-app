import 'package:flutter/material.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Settings"),
        centerTitle: true,
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: const [
          ListTile(
            title: Text("Notifications"),
            subtitle: Text("Manage notification preferences"),
            trailing: Icon(Icons.chevron_right),
          ),
          ListTile(
            title: Text("Privacy"),
            subtitle: Text("Privacy and security options"),
            trailing: Icon(Icons.chevron_right),
          ),
        ],
      ),
    );
  }
}
