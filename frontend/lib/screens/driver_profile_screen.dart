// driver_profile_screen.dart
import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/driver_service.dart';
import '../services/token_service.dart';

class DriverProfileScreen extends StatefulWidget {
  const DriverProfileScreen({super.key});

  @override
  State<DriverProfileScreen> createState() => _DriverProfileScreenState();
}

class _DriverProfileScreenState extends State<DriverProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _vehicleNumberController = TextEditingController();

  String _vehicleType = 'CAR';
  String _driverStatus = 'AVAILABLE';
  bool _saving = false;

  @override
  void dispose() {
    _vehicleNumberController.dispose();
    super.dispose();
  }

  Future<int> _resolveDriverId() async {
    final id = await TokenService.getUserId();
    if (id == null) {
      throw Exception('Missing userId/driverId. Please login again.');
    }
    return id; // assumes userId == driverId (adjust if backend differs)
  }

  Future<void> _saveProfile() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _saving = true);
    try {
      await DriverService.updateMyProfile(
        vehicleType: _vehicleType,
        vehicleNumber: _vehicleNumberController.text.trim(),
        driverStatus: _driverStatus,
      );

      if (!mounted) return;
      _snack(context, 'Profile updated');
    } catch (e) {
      if (!mounted) return;
      _snack(context, e.toString(), isError: true);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _setStatus(String status) async {
    setState(() => _saving = true);
    try {
      final driverId = await _resolveDriverId();
      await DriverService.updateDriverStatus(driverId: driverId, status: status);

      if (!mounted) return;
      setState(() => _driverStatus = status);
      _snack(context, 'Status updated: $status');
    } catch (e) {
      if (!mounted) return;
      _snack(context, e.toString(), isError: true);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Profile', style: TextStyle(fontWeight: FontWeight.w900)),
        centerTitle: true,
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(18),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.06),
                  blurRadius: 14,
                  offset: const Offset(0, 8),
                ),
              ],
            ),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Vehicle info', style: TextStyle(fontWeight: FontWeight.w900)),
                  const SizedBox(height: 10),

                  DropdownButtonFormField<String>(
                    value: _vehicleType,
                    items: const [
                      DropdownMenuItem(value: 'CAR', child: Text('CAR')),
                      DropdownMenuItem(value: 'MOTORCYCLE', child: Text('MOTORCYCLE')),
                      DropdownMenuItem(value: 'BIKE', child: Text('BIKE')),
                    ],
                    onChanged: _saving ? null : (v) => setState(() => _vehicleType = v!),
                    decoration: InputDecoration(
                      labelText: 'Vehicle type',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                  ),

                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _vehicleNumberController,
                    enabled: !_saving,
                    validator: (v) =>
                    (v == null || v.trim().isEmpty) ? 'Vehicle number is required' : null,
                    decoration: InputDecoration(
                      labelText: 'Vehicle number',
                      hintText: 'DEF 9876',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                  ),

                  const SizedBox(height: 12),
                  DropdownButtonFormField<String>(
                    value: _driverStatus,
                    items: const [
                      DropdownMenuItem(value: 'AVAILABLE', child: Text('AVAILABLE')),
                      DropdownMenuItem(value: 'BUSY', child: Text('BUSY')),
                      DropdownMenuItem(value: 'OFFLINE', child: Text('OFFLINE')),
                    ],
                    onChanged: _saving ? null : (v) => setState(() => _driverStatus = v!),
                    decoration: InputDecoration(
                      labelText: 'Driver status',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                  ),

                  const SizedBox(height: 14),
                  SizedBox(
                    height: 46,
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _saving ? null : _saveProfile,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppTheme.primaryOrange,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                      ),
                      child: _saving
                          ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                          : const Text('Save Profile', style: TextStyle(fontWeight: FontWeight.w900)),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: SizedBox(
                  height: 44,
                  child: OutlinedButton(
                    onPressed: _saving ? null : () => _setStatus('AVAILABLE'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppTheme.primaryOrange,
                      side: const BorderSide(color: AppTheme.primaryOrange),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                    child: const Text('Set Available', style: TextStyle(fontWeight: FontWeight.w900)),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: SizedBox(
                  height: 44,
                  child: OutlinedButton(
                    onPressed: _saving ? null : () => _setStatus('BUSY'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppTheme.primaryOrange,
                      side: const BorderSide(color: AppTheme.primaryOrange),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                    child: const Text('Set Busy', style: TextStyle(fontWeight: FontWeight.w900)),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  static void _snack(BuildContext context, String text, {bool isError = false}) {
    ScaffoldMessenger.of(context).clearSnackBars();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        behavior: SnackBarBehavior.floating,
        backgroundColor: isError ? const Color(0xFFC62828) : const Color(0xFF2D2D2D),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        content: Text(
          text,
          maxLines: 3,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(fontWeight: FontWeight.w800),
        ),
      ),
    );
  }
}
