import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'package:komun_nebeng/features/auth/login_screen.dart';
import 'package:komun_nebeng/features/onboarding/onboarding_screen.dart';
import 'package:komun_nebeng/features/trips/trip_screen.dart';
import 'package:komun_nebeng/features/schedule/schedule_builder_screen.dart';
import 'package:komun_nebeng/features/profile/profile_screen.dart';

import 'package:komun_nebeng/features/landing/landing_screen.dart';

import 'package:komun_nebeng/features/auth/auth_provider.dart';
import 'package:komun_nebeng/features/onboarding/onboarding_provider.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authStateProvider);
  final profileState = ref.watch(userProfileNotifierProvider);

  return GoRouter(
    initialLocation: '/',
    redirect: (context, state) {
      // If auth state is still loading, wait
      if (authState.isLoading) return null;

      final user = authState.value;
      final isLoggedIn = user != null;
      final currentLoc = state.matchedLocation;

      // Always allow root access regardless of auth state
      if (currentLoc == '/') {
        return null;
      }

      if (!isLoggedIn) {
        if (currentLoc != '/login') {
          return '/login';
        }
        return null;
      }

      // If logged in, wait for profile state to load
      if (profileState.isLoading) return null;

      final profile = profileState.value;
      final isVerified = profile != null && profile['status'] == 'verified';

      if (isVerified) {
        // If verified, navigate to trips if at login or onboarding
        if (currentLoc == '/login' || currentLoc == '/onboarding') {
          return '/trips';
        }
      } else {
        // If not verified, they must do onboarding
        if (currentLoc == '/login' || currentLoc == '/trips') {
          return '/onboarding';
        }
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const LandingScreen(),
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/onboarding',
        builder: (context, state) => const OnboardingScreen(),
      ),
      GoRoute(
        path: '/trips',
        builder: (context, state) => const TripScreen(),
      ),
      GoRoute(
        path: '/schedule',
        builder: (context, state) => const ScheduleBuilderScreen(),
      ),
      GoRoute(
        path: '/profile',
        builder: (context, state) => const ProfileScreen(),
      ),
    ],
  );
});

