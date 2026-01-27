import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../services/owner_service.dart';

class OwnerMenuScreen extends StatefulWidget {
  final int restaurantId;
  final String restaurantName;

  const OwnerMenuScreen({
    super.key,
    required this.restaurantId,
    required this.restaurantName,
  });

  @override
  State<OwnerMenuScreen> createState() => _OwnerMenuScreenState();
}

class _OwnerMenuScreenState extends State<OwnerMenuScreen> {
  late Future<List<dynamic>> _menuFuture;

  @override
  void initState() {
    super.initState();
    _menuFuture = OwnerService.getMenu(widget.restaurantId);
  }

  Future<void> _refresh() async {
    setState(() => _menuFuture = OwnerService.getMenu(widget.restaurantId));
    await _menuFuture;
  }

  Future<void> _openItemForm({Map<String, dynamic>? item}) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => _MenuItemDialog(
        item: item,
        onSubmit: (payload) async {
          if (item == null) {
            await OwnerService.addMenuItem(
              restaurantId: widget.restaurantId,
              name: payload['name'],
              description: payload['description'],
              price: payload['price'],
              category: payload['category'],
              imageUrl: payload['imageUrl'],
              isAvailable: payload['isAvailable'],
            );
          } else {
            await OwnerService.updateMenuItem(
              restaurantId: widget.restaurantId,
              itemId: (item['id'] as num).toInt(),
              name: payload['name'],
              description: payload['description'],
              price: payload['price'],
              category: payload['category'],
              imageUrl: payload['imageUrl'],
              isAvailable: payload['isAvailable'],
            );
          }
        },
      ),
    );

    if (ok == true) await _refresh();
  }

  Future<void> _deleteItem(int itemId, String itemName) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Delete item?'),
        content: Text('Are you sure you want to delete "$itemName"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          TextButton(
            style: TextButton.styleFrom(
              foregroundColor: const Color(0xFFC62828),
            ),
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (ok != true) return;

    try {
      await OwnerService.deleteMenuItem(widget.restaurantId, itemId);
      await _refresh();
      if (!context.mounted) return;
      _snack(context, "Item deleted successfully");
    } catch (e) {
      if (!context.mounted) return;
      _snack(context, e.toString(), isError: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Menu • ${widget.restaurantName}",
            style: const TextStyle(fontWeight: FontWeight.w900)),
        centerTitle: true,
      ),
      floatingActionButton: FloatingActionButton(
        backgroundColor: AppTheme.primaryOrange,
        onPressed: () => _openItemForm(),
        child: const Icon(Icons.add_rounded, color: Colors.white),
      ),
      body: RefreshIndicator(
        onRefresh: _refresh,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          children: [
            FutureBuilder<List<dynamic>>(
              future: _menuFuture,
              builder: (context, snap) {
                if (snap.connectionState == ConnectionState.waiting) {
                  return _loadingBox(height: 200);
                }
                if (snap.hasError) {
                  return _errorCard("Failed to load menu", snap.error.toString());
                }
                final list = snap.data ?? [];
                if (list.isEmpty) {
                  return _empty("No menu items", "Add your first item.");
                }

                return Column(
                  children: list.map((raw) {
                    final m = (raw as Map).cast<String, dynamic>();
                    final id = (m['id'] as num).toInt();
                    final name = (m['name'] ?? '').toString();
                    final desc = (m['description'] ?? '').toString();
                    final category = (m['category'] ?? '').toString();
                    final price = (m['price'] ?? 0).toString();
                    final isAvailable = (m['isAvailable'] ?? false) == true;

                    return Container(
                      margin: const EdgeInsets.only(bottom: 12),
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(16),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.05),
                            blurRadius: 10,
                            offset: const Offset(0, 6),
                          )
                        ],
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Expanded(
                                child: Text(name,
                                    style: const TextStyle(
                                        fontWeight: FontWeight.w900, fontSize: 16)),
                              ),
                              _pill(
                                text: isAvailable ? "AVAILABLE" : "UNAVAILABLE",
                                color: isAvailable
                                    ? const Color(0xFF2E7D32)
                                    : const Color(0xFFC62828),
                              ),
                            ],
                          ),
                          const SizedBox(height: 6),
                          Text("$category • $price EGP",
                              style:
                              TextStyle(color: Colors.grey[700], fontWeight: FontWeight.w800)),
                          if (desc.trim().isNotEmpty) ...[
                            const SizedBox(height: 6),
                            Text(desc, style: TextStyle(color: Colors.grey[800])),
                          ],
                          const SizedBox(height: 12),
                          Row(
                            children: [
                              Expanded(
                                child: SizedBox(
                                  height: 42,
                                  child: ElevatedButton.icon(
                                    onPressed: () => _openItemForm(item: m),
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: AppTheme.primaryOrange,
                                      foregroundColor: Colors.white,
                                      shape: RoundedRectangleBorder(
                                          borderRadius: BorderRadius.circular(14)),
                                    ),
                                    icon: const Icon(Icons.edit_rounded, size: 18),
                                    label: const Text("Edit item",
                                        style: TextStyle(fontWeight: FontWeight.w900)),
                                  ),
                                ),
                              ),
                              const SizedBox(width: 10),
                              SizedBox(
                                height: 42,
                                child: IconButton(
                                  tooltip: 'Delete',
                                  style: IconButton.styleFrom(
                                    backgroundColor: const Color(0xFFC62828).withOpacity(0.1),
                                    foregroundColor: const Color(0xFFC62828),
                                  ),
                                  onPressed: () => _deleteItem(id, name),
                                  icon: const Icon(Icons.delete_rounded),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    );
                  }).toList(),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  static Widget _pill({required String text, required Color color}) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
    decoration: BoxDecoration(
      color: color.withOpacity(0.12),
      borderRadius: BorderRadius.circular(14),
    ),
    child: Text(text,
        style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: 12)),
  );

  static Widget _loadingBox({required double height}) => Container(
    height: height,
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: const Center(child: CircularProgressIndicator()),
  );

  static Widget _errorCard(String title, String message) => Container(
    padding: const EdgeInsets.all(16),
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
        const SizedBox(height: 8),
        Text(message, style: const TextStyle(color: Colors.red)),
      ],
    ),
  );

  static Widget _empty(String title, String subtitle) => Container(
    padding: const EdgeInsets.all(18),
    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
    child: Column(
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
        const SizedBox(height: 6),
        Text(subtitle, textAlign: TextAlign.center, style: TextStyle(color: Colors.grey[700])),
      ],
    ),
  );

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

class _MenuItemDialog extends StatefulWidget {
  final Map<String, dynamic>? item;
  final Future<void> Function(Map<String, dynamic> payload) onSubmit;

  const _MenuItemDialog({required this.item, required this.onSubmit});

  @override
  State<_MenuItemDialog> createState() => _MenuItemDialogState();
}

class _MenuItemDialogState extends State<_MenuItemDialog> {
  final _formKey = GlobalKey<FormState>();

  late final TextEditingController _name;
  late final TextEditingController _description;
  late final TextEditingController _category;
  late final TextEditingController _price;
  late final TextEditingController _imageUrl;
  bool _isAvailable = true;

  bool _saving = false;

  @override
  void initState() {
    super.initState();
    final m = widget.item;
    _name = TextEditingController(text: (m?['name'] ?? '').toString());
    _description = TextEditingController(text: (m?['description'] ?? '').toString());
    _category = TextEditingController(text: (m?['category'] ?? '').toString());
    _price = TextEditingController(text: (m?['price'] ?? '').toString());
    _imageUrl = TextEditingController(text: (m?['imageUrl'] ?? '').toString());
    _isAvailable = (m?['isAvailable'] ?? true) == true;
  }

  @override
  void dispose() {
    _name.dispose();
    _description.dispose();
    _category.dispose();
    _price.dispose();
    _imageUrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _saving = true);
    try {
      final payload = <String, dynamic>{
        'name': _name.text.trim(),
        'description': _description.text.trim(),
        'category': _category.text.trim(),
        'price': double.parse(_price.text.trim()),
        'imageUrl': _imageUrl.text.trim().isEmpty ? null : _imageUrl.text.trim(),
        'isAvailable': _isAvailable,
      };
      await widget.onSubmit(payload);

      if (!mounted) return;
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error: ${e.toString()}'),
          backgroundColor: const Color(0xFFC62828),
        ),
      );
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEdit = widget.item != null;
    return AlertDialog(
      title: Text(isEdit ? 'Edit menu item' : 'Add menu item'),
      content: SizedBox(
        width: 420,
        child: Form(
          key: _formKey,
          child: SingleChildScrollView(
            child: Column(
              children: [
                _field(_name, "Name", requiredField: true),
                const SizedBox(height: 10),
                _field(_category, "Category", requiredField: true),
                const SizedBox(height: 10),
                _field(_price, "Price", requiredField: true, keyboardType: TextInputType.number),
                const SizedBox(height: 10),
                _field(_imageUrl, "Image URL"),
                const SizedBox(height: 10),
                _field(_description, "Description", requiredField: true, maxLines: 3),
                const SizedBox(height: 10),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  value: _isAvailable,
                  onChanged: _saving ? null : (v) => setState(() => _isAvailable = v),
                  title: const Text('Available'),
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(onPressed: _saving ? null : () => Navigator.pop(context, false), child: const Text('Cancel')),
        TextButton(onPressed: _saving ? null : _submit, child: Text(_saving ? 'Saving...' : 'Save')),
      ],
    );
  }

  static Widget _field(
      TextEditingController c,
      String label, {
        bool requiredField = false,
        int maxLines = 1,
        TextInputType keyboardType = TextInputType.text,
      }) {
    return TextFormField(
      controller: c,
      keyboardType: keyboardType,
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
}