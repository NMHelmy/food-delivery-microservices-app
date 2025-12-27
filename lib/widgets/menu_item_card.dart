import 'package:flutter/material.dart';
import '../models/menu_item.dart';
import '../theme/app_theme.dart';

class MenuItemCard extends StatelessWidget {
  final MenuItem item;
  final VoidCallback? onTap;

  const MenuItemCard({
    super.key,
    required this.item,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final disabled = !item.isAvailable;

    return Opacity(
      opacity: disabled ? 0.55 : 1,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.05),
                blurRadius: 10,
                offset: const Offset(0, 6),
              ),
            ],
          ),
          child: Row(
            children: [
              // Image (if available)
              ClipRRect(
                borderRadius: BorderRadius.circular(14),
                child: SizedBox(
                  width: 82,
                  height: 82,
                  child: ((item.imageUrl ?? '').isNotEmpty)
                      ? Image.network(
                    item.imageUrl!,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => _placeholder(),
                  )
                      : _placeholder(),
                ),
              ),

              const SizedBox(width: 12),

              // Texts
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item.name,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                      ),
                    ),

                    if ((item.description ?? '').isNotEmpty) ...[
                      const SizedBox(height: 6),
                      Text(
                        item.description!,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          color: Colors.grey[600],
                          fontSize: 13,
                          height: 1.2,
                        ),
                      ),
                    ],

                    const SizedBox(height: 10),

                    Row(
                      children: [
                        Text(
                          "${item.price.toStringAsFixed(2)} EGP",
                          style: const TextStyle(
                            color: AppTheme.primaryOrange,
                            fontWeight: FontWeight.w900,
                            fontSize: 14,
                          ),
                        ),
                        const Spacer(),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 8,
                          ),
                          decoration: BoxDecoration(
                            color: disabled
                                ? Colors.grey.shade400
                                : AppTheme.primaryOrange,
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            disabled ? "Unavailable" : "+",
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.w800,
                              fontSize: 12,
                            ),
                          ),
                        ),
                      ],
                    )
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _placeholder() {
    return Container(
      color: AppTheme.primaryOrange.withOpacity(0.12),
      child: const Center(
        child: Icon(
          Icons.fastfood_rounded,
          color: AppTheme.primaryOrange,
          size: 28,
        ),
      ),
    );
  }
}
