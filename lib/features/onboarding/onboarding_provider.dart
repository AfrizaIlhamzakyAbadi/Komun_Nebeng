import 'dart:async';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../auth/auth_provider.dart';
import '../trips/trip_provider.dart'; // To call autoGenerateTrips

class UserProfileNotifier extends AsyncNotifier<Map<String, dynamic>?> {
  @override
  FutureOr<Map<String, dynamic>?> build() async {
    final user = ref.watch(authStateProvider).value;
    if (user == null) {
      return null;
    }
    final doc = await FirebaseFirestore.instance.collection('users').doc(user.uid).get();
    if (doc.exists) {
      return doc.data();
    } else {
      return null;
    }
  }

  Future<void> fetchProfile() async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final user = ref.read(authStateProvider).value;
      if (user == null) {
        return null;
      }
      final doc = await FirebaseFirestore.instance.collection('users').doc(user.uid).get();
      if (doc.exists) {
        return doc.data();
      } else {
        return null;
      }
    });
  }

  // Module 1: Flexible Profile Scheduling & Auto-Trip Generation
  Future<void> submitProfile({
    required String username,
    required String socialHandle,
    required List<String> defaultRoles,
    required String homeGeohash,
    required Map<String, List<Map<String, String>>> schedule,
  }) async {
    final user = ref.read(authStateProvider).value;
    if (user == null) return;

    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final profileData = <String, dynamic>{
        'username': username,
        'social_handle': socialHandle,
        'status': 'pending_verification',
        'rejection_reason': null,
        'default_roles': defaultRoles,
        'home_geohash': homeGeohash,
      };

      // Add the 7 day fields if they exist
      final days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
      for (final day in days) {
        final dayKey = 'schedule_${day.toLowerCase()}';
        if (schedule.containsKey(day) && schedule[day]!.isNotEmpty) {
          profileData[dayKey] = schedule[day];
        } else {
          profileData[dayKey] = null; // Clear if not set
        }
      }

      await FirebaseFirestore.instance.collection('users').doc(user.uid).set(profileData);
      
      // Auto-generate trips after saving profile
      await ref.read(tripActionProvider.notifier).autoGenerateTrips(profileData);

      return profileData;
    });
  }
}

final userProfileNotifierProvider = AsyncNotifierProvider<UserProfileNotifier, Map<String, dynamic>?>(UserProfileNotifier.new);
