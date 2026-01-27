import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../providers/auth_provider.dart';
import '../theme/app_theme.dart';
import '../widgets/tabs_factory.dart';
import '../widgets/app_tab.dart';

import 'orders_screen.dart';

class MainNavigationScreen extends StatefulWidget {
  const MainNavigationScreen({super.key});

  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen>
    with TickerProviderStateMixin {
  int _currentIndex = 0;

  late final AnimationController _animationController;
  late final Animation<double> _fadeAnimation;

  // Only used if the current role includes Orders tab
  final GlobalKey<OrdersScreenState> _ordersKey = GlobalKey<OrdersScreenState>();

  @override
  void initState() {
    super.initState();

    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 250),
    );
    _fadeAnimation = CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInOut,
    );

    _animationController.forward();
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  List<AppTab> _buildTabsForRole(String role) {
    final tabs = tabsForRole(role).cast<AppTab>(); // from tabs_factory.dart [file:30]

    // Inject the Orders key only when OrdersScreen is present (customer role)
    for (var i = 0; i < tabs.length; i++) {
      final t = tabs[i];
      if (t.page is OrdersScreen) {
        tabs[i] = AppTab(
          label: t.label,
          icon: t.icon,
          page: OrdersScreen(key: _ordersKey),
        );
      }
    }

    return tabs;
  }

  void _onTabSelected(int index, List<AppTab> tabs) {
    if (index == _currentIndex) return;

    final selected = tabs[index];

    // Refresh Orders when navigating to it (customer only)
    if (selected.page is OrdersScreen || selected.label == "Orders") {
      _ordersKey.currentState?.refresh(); // OrdersScreenState.refresh() [file:10]
    }

    _animationController.reverse().then((_) {
      if (!mounted) return;
      setState(() => _currentIndex = index);
      _animationController.forward();
    });
  }

  @override
  Widget build(BuildContext context) {
    final role = context.watch<AuthProvider>().role; // [file:3]
    final tabs = _buildTabsForRole(role);

    // If role changes (or tabs count changes), keep index in range
    if (_currentIndex >= tabs.length) {
      _currentIndex = 0;
    }

    return Scaffold(
      body: FadeTransition(
        opacity: _fadeAnimation,
        child: IndexedStack(
          index: _currentIndex,
          children: tabs.map((t) => t.page).toList(),
        ),
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (i) => _onTabSelected(i, tabs),
        selectedItemColor: AppTheme.primaryOrange,
        unselectedItemColor: Colors.grey,
        showUnselectedLabels: true,
        type: BottomNavigationBarType.fixed,
        items: tabs
            .map(
              (t) => BottomNavigationBarItem(
            icon: Icon(t.icon),
            activeIcon: Icon(t.icon),
            label: t.label,
          ),
        )
            .toList(),
      ),
    );
  }
}
