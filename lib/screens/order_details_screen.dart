import 'package:flutter/material.dart';

class OrderDetailsScreen extends StatelessWidget {
  final Map<String, dynamic> order;

  const OrderDetailsScreen({super.key, required this.order});

  @override
  Widget build(BuildContext context) {
    final items = order['items'] as List<dynamic>;

    return Scaffold(
      appBar: AppBar(
        title: Text("Order #${order['id']}"),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Restaurant + Status
            Text(
              order['restaurantName'],
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              "Status: ${order['status']} • Payment: ${order['paymentStatus']}",
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

            ...items.map((item) {
              return Container(
                margin: const EdgeInsets.only(bottom: 12),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Text(
                        "${item['quantity']} × ${item['itemName']}",
                        style: const TextStyle(fontSize: 14),
                      ),
                    ),
                    Text(
                      "${item['price']} EGP",
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

            _priceRow(
              "Total",
              order['total'],
              isTotal: true,
            ),

            if (order['specialInstructions'] != null) ...[
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
                order['specialInstructions'],
                style: TextStyle(color: Colors.grey.shade700),
              ),
            ],

            const SizedBox(height: 24),

            Text(
              "Estimated delivery: ${order['estimatedDeliveryTime']}",
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
          "$value EGP",
          style: TextStyle(
            fontSize: isTotal ? 15 : 13,
            fontWeight: isTotal ? FontWeight.w600 : FontWeight.normal,
          ),
        ),
      ],
    );
  }
}
