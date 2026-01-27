import 'package:flutter/material.dart';
import '../services/order_service.dart';

class OrderDetailsScreen extends StatefulWidget {
  /// You can pass either:
  /// - order: full map (fast initial render)
  /// - orderId: to fetch from backend (recommended after payment)
  final Map<String, dynamic>? order;
  final int? orderId;

  const OrderDetailsScreen({
    super.key,
    this.order,
    this.orderId,
  }) : assert(order != null || orderId != null,
  "Provide either order or orderId");

  @override
  State<OrderDetailsScreen> createState() => _OrderDetailsScreenState();
}

class _OrderDetailsScreenState extends State<OrderDetailsScreen> {
  late Future<Map<String, dynamic>> _future;

  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  Future<Map<String, dynamic>> _load() async {
    // If orderId is provided, always fetch the latest from backend
    // (ensures paymentStatus reflects CARD confirmation).
    if (widget.orderId != null) {
      return OrdersService.getOrderById(widget.orderId!);
    }

    // If only "order" was provided, use it as initial data.
    // Still, it’s usually better to refresh after payment.
    return widget.order!;
  }

  Future<void> _refresh() async {
    setState(() {
      _future = _load();
    });
    await _future;
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Map<String, dynamic>>(
      future: _future,
      builder: (context, snapshot) {
        final title = snapshot.hasData
            ? "Order #${snapshot.data!['id']}"
            : "Order";

        return Scaffold(
          appBar: AppBar(
            title: Text(title),
            centerTitle: true,
            actions: [
              IconButton(
                onPressed: _refresh,
                icon: const Icon(Icons.refresh),
              ),
            ],
          ),
          body: _buildBody(snapshot),
        );
      },
    );
  }

  Widget _buildBody(AsyncSnapshot<Map<String, dynamic>> snapshot) {
    if (snapshot.connectionState == ConnectionState.waiting) {
      return const Center(child: CircularProgressIndicator());
    }

    if (snapshot.hasError) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Text(
            "Failed to load order.\n${snapshot.error}",
            textAlign: TextAlign.center,
          ),
        ),
      );
    }

    final order = snapshot.data!;
    final items = (order['items'] as List?) ?? const [];

    final restaurantName = (order['restaurantName'] ?? "Restaurant").toString();
    final status = (order['status'] ?? "-").toString();
    final paymentStatus = (order['paymentStatus'] ?? "-").toString();
    final paymentMethod = (order['paymentMethod'] ?? "-").toString();

    return RefreshIndicator(
      onRefresh: _refresh,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Restaurant + Status
            Text(
              restaurantName,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              "Status: $status • Payment: $paymentStatus",
              style: TextStyle(
                fontSize: 13,
                color: Colors.grey.shade700,
              ),
            ),

            const SizedBox(height: 24),

            // Items
            const Text(
              "Items",
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),

            if (items.isEmpty)
              Text(
                "No items found.",
                style: TextStyle(color: Colors.grey.shade700),
              )
            else
              ...items.map((raw) {
                final item = (raw as Map).cast<String, dynamic>();
                final qty = item['quantity'];
                final name = item['itemName'] ?? "";
                final price = item['price'];

                return Container(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(
                          "$qty × $name",
                          style: const TextStyle(fontSize: 14),
                        ),
                      ),
                      Text(
                        "$price EGP",
                        style: const TextStyle(fontWeight: FontWeight.w500),
                      ),
                    ],
                  ),
                );
              }).toList(),

            const Divider(height: 32),

            _priceRow("Subtotal", order['subtotal']),
            _priceRow("Delivery fee", order['deliveryFee']),
            _priceRow("Tax", order['tax']),
            const SizedBox(height: 8),
            _priceRow("Total", order['total'], isTotal: true),

            if (order['specialInstructions'] != null &&
                order['specialInstructions'].toString().trim().isNotEmpty) ...[
              const SizedBox(height: 24),
              const Text(
                "Special instructions",
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 6),
              Text(
                order['specialInstructions'].toString(),
                style: TextStyle(color: Colors.grey.shade700),
              ),
            ],

            const SizedBox(height: 24),

            const Text(
              "Payment method",
              style: TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              paymentMethod,
              style: TextStyle(color: Colors.grey.shade700),
            ),

            const SizedBox(height: 24),

            Text(
              "Estimated delivery: ${order['estimatedDeliveryTime'] ?? '-'}",
              style: TextStyle(
                fontSize: 12,
                color: Colors.grey.shade600,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _priceRow(String label, dynamic value, {bool isTotal = false}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: isTotal ? 15 : 13,
            fontWeight: isTotal ? FontWeight.w600 : FontWeight.normal,
          ),
        ),
        Text(
          "${value ?? '-'} EGP",
          style: TextStyle(
            fontSize: isTotal ? 15 : 13,
            fontWeight: isTotal ? FontWeight.w600 : FontWeight.normal,
          ),
        ),
      ],
    );
  }
}
