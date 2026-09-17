import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:komun_nebeng/core/routing/app_router.dart';
import 'package:komun_nebeng/theme/app_theme.dart';
import 'firebase_options.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );

  runApp(
    const ProviderScope(
      child: KomunNebengApp(),
    ),
  );
}

class KomunNebengApp extends ConsumerWidget {
  const KomunNebengApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(appRouterProvider);

    return MaterialApp.router(
      title: 'Komun_Nebeng_1.4',
      theme: AppTheme.darkTheme, // We only use dark theme for this app
      routerConfig: router,
      debugShowCheckedModeBanner: false,
    );
  }
}
