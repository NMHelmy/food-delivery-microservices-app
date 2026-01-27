import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/owner_service.dart';

class OwnerRestaurantFormScreen extends StatefulWidget {
  final Map<String, dynamic>? restaurant; // if provided => edit

  const OwnerRestaurantFormScreen({super.key, this.restaurant});

  @override
  State<OwnerRestaurantFormScreen> createState() => _OwnerRestaurantFormScreenState();
}

class _OwnerRestaurantFormScreenState extends State<OwnerRestaurantFormScreen> {
  final _formKey = GlobalKey<FormState>();

  late final TextEditingController _name;
  late final TextEditingController _description;
  late final TextEditingController _cuisine;
  late final TextEditingController _address;
  late final TextEditingController _city;
  late final TextEditingController _district;
  late final TextEditingController _phone;
  late final TextEditingController _imageUrl;

  bool _saving = false;

  bool get isEdit => widget.restaurant != null;

  @override
  void initState() {
    super.initState();
    final r = widget.restaurant;

    _name = TextEditingController(text: (r?['name'] ?? '').toString());
    _description = TextEditingController(text: (r?['description'] ?? '').toString());
    _cuisine = TextEditingController(text: (r?['cuisine'] ?? '').toString());
    _address = TextEditingController(text: (r?['address'] ?? '').toString());
    _phone = TextEditingController(text: (r?['phone'] ?? '').toString());
    _imageUrl = TextEditingController(text: (r?['imageUrl'] ?? '').toString());

    // Backend response doesn't return city/district, so keep blank on edit unless you store them.
    _city = TextEditingController(text: '');
    _district = TextEditingController(text: '');
  }

  @override
  void dispose() {
    _name.dispose();
    _description.dispose();
    _cuisine.dispose();
    _address.dispose();
    _city.dispose();
    _district.dispose();
    _phone.dispose();
    _imageUrl.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _saving = true);
    try {
      if (isEdit) {
        final id = (widget.restaurant!['id'] as num).toInt();
        await OwnerService.updateRestaurant(
          restaurantId: id,
          name: _name.text.trim(),
          description: _description.text.trim(),
          cuisineType: _cuisine.text.trim(),
          address: _address.text.trim(),
          phone: _phone.text.trim().isEmpty ? null : _phone.text.trim(),
          logoUrl: _imageUrl.text.trim().isEmpty ? null : _imageUrl.text.trim(),
        );
      } else {
        await OwnerService.createRestaurant(
          name: _name.text.trim(),
          description: _description.text.trim(),
          cuisineType: _cuisine.text.trim(),
          address: _address.text.trim(),
          city: _city.text.trim(),
          district: _district.text.trim(),
          phone: _phone.text.trim().isEmpty ? null : _phone.text.trim(),
          logoUrl: _imageUrl.text.trim().isEmpty ? null : _imageUrl.text.trim(),
        );
      }

      if (!mounted) return;
      Navigator.pop(context, true);
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
        title: Text(isEdit ? "Edit Restaurant" : "Create Restaurant",
            style: const TextStyle(fontWeight: FontWeight.w900)),
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
                )
              ],
            ),
            child: Form(
              key: _formKey,
              child: Column(
                children: [
                  _field(_name, label: "Name", requiredField: true),
                  const SizedBox(height: 12),
                  _field(_cuisine, label: "Cuisine", requiredField: true),
                  const SizedBox(height: 12),
                  _field(_address, label: "Address", requiredField: true),
                  const SizedBox(height: 12),
                  if (!isEdit) ...[
                    _field(_city, label: "City", requiredField: true),
                    const SizedBox(height: 12),
                    _field(_district, label: "District", requiredField: true),
                    const SizedBox(height: 12),
                  ],
                  _field(_phone, label: "Phone"),
                  const SizedBox(height: 12),
                  _field(_imageUrl, label: "Image URL"),
                  const SizedBox(height: 12),
                  _field(_description,
                      label: "Description", requiredField: true, maxLines: 3),
                  const SizedBox(height: 14),
                  SizedBox(
                    height: 46,
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _saving ? null : _save,
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
                          : const Text("Save", style: TextStyle(fontWeight: FontWeight.w900)),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  static Widget _field(
      TextEditingController c, {
        required String label,
        bool requiredField = false,
        int maxLines = 1,
      }) {
    return TextFormField(
      controller: c,
      maxLines: maxLines,
      validator: (v) {
        if (!requiredField) return null;
        return (v == null || v.trim().isEmpty) ? "$label is required" : null;
      },
      decoration: InputDecoration(
        labelText: label,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
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
