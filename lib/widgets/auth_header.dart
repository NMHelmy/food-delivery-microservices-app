import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class AuthHeader extends StatelessWidget {
  final String backLabel;
  final String title;
  final String subtitle;

  const AuthHeader({
    super.key,
    required this.backLabel,
    required this.title,
    required this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 270,
      child: ClipPath(
        clipper: _WaveClipper(),
        child: Stack(
          children: [
            Container(
              width: double.infinity,
              color: AppTheme.primaryOrange,
            ),

            Positioned(
              right: -40,
              top: 30,
              child: Icon(
                Icons.fastfood,
                size: 180,
                color: Colors.white.withOpacity(0.2),
              ),
            ),

            // Content
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 52, 16, 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      InkWell(
                        onTap: () => Navigator.of(context).maybePop(),
                        child: const Icon(
                          Icons.arrow_back,
                          color: Colors.white,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        backLabel,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 35),

                  Text(
                    title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 25,
                      fontWeight: FontWeight.w700,
                    ),
                  ),

                  const SizedBox(height: 6),

                  Text(
                    subtitle,
                    style: const TextStyle(
                      color: Colors.white70,
                      fontSize: 16,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _WaveClipper extends CustomClipper<Path> {
  @override
  Path getClip(Size size) {
    final path = Path();
    path.lineTo(0, size.height * 0.75);
    path.quadraticBezierTo(
      size.width * 0.25,
      size.height * 0.90,
      size.width * 0.50,
      size.height * 0.82,
    );
    path.quadraticBezierTo(
      size.width * 0.75,
      size.height * 0.70,
      size.width,
      size.height * 0.82,
    );
    path.lineTo(size.width, 0);
    path.close();
    return path;
  }

  @override
  bool shouldReclip(CustomClipper<Path> oldClipper) => false;
}
