import 'dart:async';
import 'dart:isolate';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/services.dart';
import 'package:latlong2/latlong.dart';
import 'package:dart_geohash/dart_geohash.dart';


import '../../core/routing/astar_isolate.dart';
import '../../core/routing/geohash_generator.dart';
import '../../core/routing/polyline_encoder.dart';
import '../../core/repositories/campus_repository.dart';
import '../auth/auth_provider.dart';

// Module 2: Day-to-int helper for chronological sorting
const _dayOrder = {
  'Monday': 1,
  'Tuesday': 2,
  'Wednesday': 3,
  'Thursday': 4,
  'Friday': 5,
  'Saturday': 6,
  'Sunday': 7,
};

String getGeoJsonPathForCampus(String campusId) {
  if (campusId.isEmpty) return 'assets/surabaya_roads.geojson';
  try {
    final campus = CampusRepository.allCampuses.firstWhere(
      (c) => c.id == campusId, 
      orElse: () => CampusRepository.allCampuses.first
    );
    if (campus.region == 'Yogyakarta') return 'assets/yogyakarta_roads.geojson';
    if (campus.region == 'Jabodetabek') return 'assets/jakarta_roads.geojson'; // Fixed file name
  } catch (e) {
    print("Region mapping error: $e");
  }
  return 'assets/surabaya_roads.geojson';
}

String guessGeoJsonPathFromHashes(String hash1, String hash2) {
  for (final campus in CampusRepository.allCampuses) {
    if (campus.geohashes.contains(hash1) || campus.geohashes.contains(hash2)) {
      if (campus.region == 'Yogyakarta') return 'assets/yogyakarta_roads.geojson';
      if (campus.region == 'Jabodetabek') return 'assets/jakarta_roads.geojson';
      return 'assets/surabaya_roads.geojson';
    }
  }
  return 'assets/surabaya_roads.geojson';
}

final tripsProvider = StreamProvider.autoDispose<List<Map<String, dynamic>>>((ref) {
  final user = ref.watch(authStateProvider).value;
  if (user == null) return Stream.value([]);

  return FirebaseFirestore.instance
      .collection('trips')
      .where('participants', arrayContains: user.uid)
      .snapshots()
      .map((snapshot) {
        final trips = snapshot.docs
            .map((doc) => {'id': doc.id, ...doc.data()})
            .where((trip) => trip['status'] != 'hidden_adopted')
            .toList();
        // Module 2: Sort chronologically by day then time
        trips.sort((a, b) {
          final dayA = _dayOrder[a['day_of_week']] ?? 8;
          final dayB = _dayOrder[b['day_of_week']] ?? 8;
          if (dayA != dayB) return dayA.compareTo(dayB);
          return (a['schedule_time'] as String? ?? '').compareTo(b['schedule_time'] as String? ?? '');
        });
        return trips;
      });
});

class TripActionNotifier extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  Future<void> acceptRequest(String tripId) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await FirebaseFirestore.instance.collection('trips').doc(tripId).update({
        'status': 'connected',
      });
      // Force refresh
      ref.invalidate(tripsProvider);
    });
  }



  // Admin approves via cloud function or another admin panel, but assuming simulated local:
  Future<void> adminApproveSimulated(String tripId, String driverId, String hitchhikerId) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final batch = FirebaseFirestore.instance.batch();
      
      final tripRef = FirebaseFirestore.instance.collection('trips').doc(tripId);
      batch.update(tripRef, {'status': 'connected'});
      
      // Simulate locking & resolving conflicts by updating user docs
      // ... (omitted for brevity, typically handled server-side)

      await batch.commit();

      // Force refresh
      ref.invalidate(tripsProvider);
    });
  }

  Future<void> syncStatus(String tripId, String newStatus) async {
    await FirebaseFirestore.instance.collection('trips').doc(tripId).update({
      'status': newStatus,
    });
  }

  // Module 1: Auto-generate trips based on flexible profile arrays
  Future<void> autoGenerateTrips(Map<String, dynamic> profileData) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final user = ref.read(authStateProvider).value;
      if (user == null) return;

      // 1. Delete all existing 'searching' trips for this user
      final batch = FirebaseFirestore.instance.batch();
      final existingTrips = await FirebaseFirestore.instance
          .collection('trips')
          .where('uid', isEqualTo: user.uid)
          .get(); // Removed the status .where() clause!

      for (final doc in existingTrips.docs) {
        if (doc.data()['status'] == 'searching') {
          batch.delete(doc.reference);
        }
      }
      await batch.commit();

      // 2. Extract profile fields
      final roles = (profileData['default_roles'] as List<dynamic>?)?.cast<String>() ?? [];
      final homeGeohash = profileData['home_geohash'] as String?;

      if (roles.isEmpty || homeGeohash == null) return;

      final geoHasher = GeoHasher();
      final coords = geoHasher.decode(homeGeohash);
      final mapLocation = LatLng(coords[1], coords[0]); // [lng, lat]



      // Helper to generate a trip
      Future<void> createGeneratedTrip(String day, Map<String, dynamic> tripConfig) async {
        final type = tripConfig['type'] as String;
        final time = tripConfig['time'] as String;
        final depCampus = tripConfig['departure_campus'] as String?;
        final arrCampus = tripConfig['arrival_campus'] as String?;

        LatLng startLatLng;
        LatLng endLatLng;
        String startGeohash;

        if (type == 'to_campus') {
          if (arrCampus == null) return;
          final campusObj = CampusRepository.allCampuses.firstWhere((c) => c.id == arrCampus, orElse: () => CampusRepository.allCampuses.first);
          final arrHash = campusObj.geohashes.first;
          final arrCoords = geoHasher.decode(arrHash);
          startLatLng = mapLocation;
          endLatLng = LatLng(arrCoords[1], arrCoords[0]);
          startGeohash = geoHasher.encode(mapLocation.longitude, mapLocation.latitude, precision: 9);
        } else if (type == 'from_campus') {
          if (depCampus == null) return;
          final campusObj = CampusRepository.allCampuses.firstWhere((c) => c.id == depCampus, orElse: () => CampusRepository.allCampuses.first);
          final depHash = campusObj.geohashes.first;
          final depCoords = geoHasher.decode(depHash);
          startLatLng = LatLng(depCoords[1], depCoords[0]);
          endLatLng = mapLocation;
          startGeohash = depHash;
        } else if (type == 'campus_to_campus') {
          if (depCampus == null || arrCampus == null) return;
          final depCampusObj = CampusRepository.allCampuses.firstWhere((c) => c.id == depCampus, orElse: () => CampusRepository.allCampuses.first);
          final arrCampusObj = CampusRepository.allCampuses.firstWhere((c) => c.id == arrCampus, orElse: () => CampusRepository.allCampuses.first);
          final depHash = depCampusObj.geohashes.first;
          final arrHash = arrCampusObj.geohashes.first;
          final depCoords = geoHasher.decode(depHash);
          final arrCoords = geoHasher.decode(arrHash);
          startLatLng = LatLng(depCoords[1], depCoords[0]);
          endLatLng = LatLng(arrCoords[1], arrCoords[0]);
          startGeohash = depHash;
        } else {
          return;
        }

        // 1. Calculate Distance
        final distanceInMeters = const Distance().as(LengthUnit.Meter, startLatLng, endLatLng);

        List<String> geohashRoute = [];

        // 2. Only route if within 100km AND user is Driver/Rider
        if (distanceInMeters <= 100000 && (roles.contains('Driver') || roles.contains('Rider'))) {
          try {
            final targetCampusId = arrCampus ?? depCampus ?? '';
            final geoJsonPath = getGeoJsonPathForCampus(targetCampusId);
            final geojsonString = await rootBundle.loadString(geoJsonPath);
            final rp = ReceivePort();
            
            await Isolate.spawn(
              aStarIsolateMain,
              AStarIsolateData(rp.sendPort, geojsonString, startLatLng, endLatLng),
            );
            
            final AStarResult? result = await rp.first;
            rp.close();
            
            if (result != null && result.run1.isNotEmpty) {
              geohashRoute = GeohashGenerator.generatePrecisionGeohashes(result.run1);
            }
          } catch (e) {
            print("Routing failed gracefully, but trip will still save: \$e");
          }
        }

        String endGeohash = geoHasher.encode(endLatLng.longitude, endLatLng.latitude, precision: 9);
        final tripData = {
          'route_geohashes': geohashRoute, // Empty if hitchhiker
          'start_geohash': startGeohash,
          'end_geohash': endGeohash,
          'trip_type': type,
          if (depCampus != null) 'departure_campus': depCampus,
          if (arrCampus != null) 'arrival_campus': arrCampus,
          'day_of_week': day,
          'schedule_time': time,
          'uid': user.uid,
          'roles_offered': roles,
          'status': 'searching',
          'participants': [user.uid],
          'pending_requests': [],
          'accepted_hitchhikers': [],
        };
        await FirebaseFirestore.instance.collection('trips').add(tripData);
      }

      // 3. Loop through 7 days
      final days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
      for (final day in days) {
        final dayKey = 'schedule_${day.toLowerCase()}';
        if (profileData[dayKey] != null) {
          final tripsArray = profileData[dayKey] as List<dynamic>;
          for (final trip in tripsArray) {
            await createGeneratedTrip(day, trip as Map<String, dynamic>);
          }
        }
      }

      ref.invalidate(tripsProvider);
    });
  }

  Future<void> saveTrip({
    required bool isToCampus,
    required LatLng mapLocation,
    required String campus,
    required String dayOfWeek,
    required String scheduleTime,
    required String role,
  }) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final user = ref.read(authStateProvider).value;
      if (user == null) throw Exception('User not logged in');

      // 1. Campus Geohashes
      final campusObj = CampusRepository.allCampuses.firstWhere((c) => c.id == campus, orElse: () => CampusRepository.allCampuses.first);
      final campusHash = campusObj.geohashes.first;
      final geoHasher = GeoHasher();
      final campusCoords = geoHasher.decode(campusHash);
      final campusLatLng = LatLng(campusCoords[1], campusCoords[0]); // decode returns [lng, lat]
      
      final startLatLng = isToCampus ? mapLocation : campusLatLng;
      final endLatLng = isToCampus ? campusLatLng : mapLocation;

      final startGeohash = geoHasher.encode(mapLocation.longitude, mapLocation.latitude, precision: 9);

      List<String> geohashRoute = [];

      if (role == 'Hitchhiker') {
        // Hitchhiker Beacon: Save start geohash without routing
        final tripData = {
          'start_geohash': startGeohash,
          'trip_type': isToCampus ? 'to_campus' : 'from_campus',
          'campus': campus,
          'day_of_week': dayOfWeek,
          'schedule_time': scheduleTime,
          'uid': user.uid,
          'roles_offered': [role],
          'status': 'searching',
          'participants': [user.uid],
        };
        await FirebaseFirestore.instance.collection('trips').add(tripData);
        ref.invalidate(tripsProvider);
        return;
      } else {
        // 2. Routing Logic for Drivers
        // 1. Distance Guardrail for UI
        final distanceInMeters = const Distance().as(LengthUnit.Meter, startLatLng, endLatLng);
        if (distanceInMeters > 100000) {
          throw Exception('Your pin is too far away. Please zoom out and move the map to the correct city!');
        }

        // 2. Load the correct Regional Map
        final geoJsonPath = getGeoJsonPathForCampus(campus);
        final geojsonString = await rootBundle.loadString(geoJsonPath);
        final receivePort = ReceivePort();
        
        await Isolate.spawn(
          aStarIsolateMain,
          AStarIsolateData(receivePort.sendPort, geojsonString, startLatLng, endLatLng),
        );
        
        final AStarResult? result = await receivePort.first;
        receivePort.close();

        if (result == null || result.run1.isEmpty) {
          throw Exception('Calculation failed. Please choose a location closer to a main road.');
        }

        // 3. High Precision Geohash Generation (Length 9)
        geohashRoute = GeohashGenerator.generatePrecisionGeohashes(result.run1);

        if (geohashRoute.isEmpty) {
          throw Exception('Calculation failed. Geohash Route is empty.');
        }
      }

      // 4. Firestore Injection for Driver
      final tripData = {
        'route_geohashes': geohashRoute,
        'start_geohash': startGeohash,
        'trip_type': isToCampus ? 'to_campus' : 'from_campus',
        'campus': campus,
        'day_of_week': dayOfWeek,
        'schedule_time': scheduleTime,
        'uid': user.uid,
        'roles_offered': [role],
        'status': 'searching',
        'participants': [user.uid],
        // Module 5: Initialize arrays
        'pending_requests': [],
        'accepted_hitchhikers': [],
      };

      await FirebaseFirestore.instance.collection('trips').add(tripData);

      ref.invalidate(tripsProvider);
    });
  }

  /// The "Hitchhiker Radar" Matching Engine for Hitchhikers to find Drivers
  Future<List<Map<String, dynamic>>> findMatchesForHitchhiker(Map<String, dynamic> hitchhikerTrip) async {
    // 1. Bulletproof Time Parser Helper (Ignores AM/PM/Spaces)
    int parseTimeToMinutes(String? timeStr) {
      if (timeStr == null || timeStr.isEmpty) return 0;
      try {
        final isPM = timeStr.toLowerCase().contains('pm');
        final isAM = timeStr.toLowerCase().contains('am');
        final cleanStr = timeStr.replaceAll(RegExp(r'[^0-9:]'), '');
        final parts = cleanStr.split(':');
        int hours = int.tryParse(parts[0]) ?? 0;
        int mins = parts.length > 1 ? (int.tryParse(parts[1]) ?? 0) : 0;
        if (isPM && hours < 12) hours += 12;
        if (isAM && hours == 12) hours = 0;
        return (hours * 60) + mins;
      } catch (e) {
        return 0; // Fallback to avoid breaking the Future
      }
    }

    final tripType = hitchhikerTrip['trip_type'] as String;
    final arrCampus = hitchhikerTrip['arrival_campus'] as String?;
    final depCampus = hitchhikerTrip['departure_campus'] as String?;
    final oldCampus = hitchhikerTrip['campus'] as String?;
    
    final targetCampusId = tripType == 'from_campus' 
       ? (depCampus ?? oldCampus) 
       : (arrCampus ?? oldCampus);

    List<String> targetGeohashes = [];
    if (targetCampusId != null) {
      final campusObj = CampusRepository.allCampuses.firstWhere((c) => c.id == targetCampusId, orElse: () => CampusRepository.allCampuses.first);
      targetGeohashes = campusObj.geohashes;
    } else {
      final detourGeohash = tripType == 'from_campus' 
          ? hitchhikerTrip['end_geohash'] as String? 
          : hitchhikerTrip['start_geohash'] as String?;
      if (detourGeohash != null) targetGeohashes = [detourGeohash];
    }
        
    final hitchhikerDay = hitchhikerTrip['day_of_week'] as String?;
    final hitchhikerMinutes = parseTimeToMinutes(hitchhikerTrip['schedule_time'] as String?);
    final currentUid = hitchhikerTrip['uid'] as String?;

    if (targetGeohashes.isEmpty) return [];

    final querySnapshot = await FirebaseFirestore.instance
        .collection('trips')
        .where('trip_type', isEqualTo: tripType)
        .get();

    final List<Map<String, dynamic>> allDocs = querySnapshot.docs.map<Map<String, dynamic>>((d) {
      return <String, dynamic>{'id': d.id, ...d.data()};
    }).toList();

    final List<Map<String, dynamic>> potentialDrivers = allDocs.where((driver) {
      // Prevent testing edge-case self-matching
      if (driver['uid'] == currentUid) return false;

      final tStatus = driver['status'] as String?;
      if (tStatus != 'searching' && tStatus != 'active' && tStatus != 'connected') return false;

      // CRITICAL FIX: Must accept both 'Driver' and 'Rider' terminology
      final roles = driver['roles_offered'] as List<dynamic>?;
      if (roles == null || (!roles.contains('Driver') && !roles.contains('Rider'))) {
        return false;
      }

      final tArr = driver['arrival_campus'] as String?;
      final tDep = driver['departure_campus'] as String?;
      final tOld = driver['campus'] as String?;
      
      final dArr = tArr ?? tOld;
      final dDep = tDep ?? tOld;
      
      final hArr = arrCampus ?? oldCampus;
      final hDep = depCampus ?? oldCampus;
      
      if (dArr != hArr || dDep != hDep) return false;

      final tDay = driver['day_of_week'] as String?;
      if (tDay != hitchhikerDay) return false;

      final driverMinutes = parseTimeToMinutes(driver['schedule_time'] as String?);

      if (tripType == 'to_campus') {
        if (driverMinutes > hitchhikerMinutes) {
          return false;
        }
      } else {
        if (driverMinutes < hitchhikerMinutes) {
          return false;
        }
      }

      return true;
    }).toList();

    final List<Map<String, dynamic>> matchedDrivers = [];
    final geoHasher = GeoHasher();

    for (final driver in potentialDrivers) {
      bool isLocationMatch = false;

      for (final detourGeohash in targetGeohashes) {
        int maxLen = detourGeohash.length < 9 ? detourGeohash.length : 9;
        
        for (int len = maxLen; len >= 5; len--) {
          String hBase = detourGeohash.substring(0, len);
          final neighbors = geoHasher.neighbors(hBase).values.toList();
          final targetNineList = [hBase, ...neighbors];
          
          final routeGeohashes = (driver['route_geohashes'] as List<dynamic>?)?.cast<String>() ?? [];
          bool matched = routeGeohashes.any((String dHash) {
            if (dHash.length >= len) {
              return targetNineList.contains(dHash.substring(0, len));
            }
            return false;
          });

          if (matched) {
            isLocationMatch = true;
            break;
          }
        }
        if (isLocationMatch) break;
      }

      if (isLocationMatch) {
        matchedDrivers.add(driver);
      }
    }

    // Module 2: Similarity Ranking
    final hDoc = await FirebaseFirestore.instance.collection('users').doc(currentUid).get();
    final hData = hDoc.data();

    for (final driver in matchedDrivers) {
      int similarityScore = 0;
      final dUid = driver['uid'];
      if (dUid != null && hData != null) {
        final dDoc = await FirebaseFirestore.instance.collection('users').doc(dUid).get();
        final dData = dDoc.data();
        if (dData != null) {
          final days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
          for (final day in days) {
            final dayKey = 'schedule_${day.toLowerCase()}';
            final hTrips = hData[dayKey] as List<dynamic>? ?? [];
            final dTrips = dData[dayKey] as List<dynamic>? ?? [];
            for (final hT in hTrips) {
              final hMap = hT as Map<String, dynamic>;
              for (final dT in dTrips) {
                final dMap = dT as Map<String, dynamic>;
                if (hMap['type'] == dMap['type'] && 
                    hMap['departure_campus'] == dMap['departure_campus'] && 
                    hMap['arrival_campus'] == dMap['arrival_campus']) {
                  final hm = parseTimeToMinutes(hMap['time'] as String?);
                  final dm = parseTimeToMinutes(dMap['time'] as String?);
                  if ((hm - dm).abs() <= 60) {
                    similarityScore += 1;
                  }
                }
              }
            }
          }
        }
      }
      driver['similarity_score'] = similarityScore;
    }

    matchedDrivers.sort((a, b) => (b['similarity_score'] as int).compareTo(a['similarity_score'] as int));

    return matchedDrivers;
  }

  // Module 5: requestSeat uses arrayUnion
  Future<void> requestSeat(String hitchhikerTripId, String driverTripId) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final batch = FirebaseFirestore.instance.batch();
      
      final hRef = FirebaseFirestore.instance.collection('trips').doc(hitchhikerTripId);
      final dRef = FirebaseFirestore.instance.collection('trips').doc(driverTripId);

      // Hitchhiker tracks which driver they requested
      batch.update(hRef, {'status': 'request_pending', 'driver_id': driverTripId});
      // Driver gets hitchhiker added to pending_requests array
      batch.update(dRef, {
        'pending_requests': FieldValue.arrayUnion([hitchhikerTripId]),
      });
      
      await batch.commit();
      ref.invalidate(tripsProvider);
    });
  }

  // Module 5: cancelRequest uses arrayRemove
  Future<void> cancelRequest(String hitchhikerTripId, String driverTripId) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final batch = FirebaseFirestore.instance.batch();
      
      final hRef = FirebaseFirestore.instance.collection('trips').doc(hitchhikerTripId);
      final dRef = FirebaseFirestore.instance.collection('trips').doc(driverTripId);

      batch.update(hRef, {'status': 'searching', 'driver_id': FieldValue.delete()});
      batch.update(dRef, {
        'pending_requests': FieldValue.arrayRemove([hitchhikerTripId]),
      });
      
      await batch.commit();
      ref.invalidate(tripsProvider);
    });
  }
  
  /// Module 6: Run A* locally with multiple waypoints for Driver to Preview
  Future<Map<String, dynamic>?> previewWaypointRoute(
    String driverStartHash, 
    String driverEndHash, 
    String newHitchhikerHash, {
    List<String> existingWaypointHashes = const [],
  }) async {
    final geoJsonPath = guessGeoJsonPathFromHashes(driverStartHash, driverEndHash);
    final geojsonString = await rootBundle.loadString(geoJsonPath);
    final geoHasher = GeoHasher();
    
    final startCoords = geoHasher.decode(driverStartHash);
    final endCoords = geoHasher.decode(driverEndHash);
    
    final point1 = LatLng(startCoords[1], startCoords[0]);
    final point3 = LatLng(endCoords[1], endCoords[0]);

    // Build waypoints list: existing accepted + new preview hitchhiker
    final List<LatLng> allWaypoints = [];
    for (final hash in existingWaypointHashes) {
      final coords = geoHasher.decode(hash);
      allWaypoints.add(LatLng(coords[1], coords[0]));
    }
    final hCoords = geoHasher.decode(newHitchhikerHash);
    final newHitchhikerPin = LatLng(hCoords[1], hCoords[0]);
    allWaypoints.add(newHitchhikerPin);

    // Module 6: Use multi-waypoint TSP isolate
    final rp1 = ReceivePort();
    await Isolate.spawn(
      aStarIsolateMain,
      AStarIsolateData(rp1.sendPort, geojsonString, point1, point3, waypoints: allWaypoints),
    );
    final AStarResult? multiResult = await rp1.first;
    rp1.close();

    // Calculate original direct route distance for detour calculation
    final rp2 = ReceivePort();
    await Isolate.spawn(
      aStarIsolateMain,
      AStarIsolateData(rp2.sendPort, geojsonString, point1, point3),
    );
    final AStarResult? originalRes = await rp2.first;
    rp2.close();

    if (multiResult != null && originalRes != null) {
      final encodedPolyline = PolylineEncoder.encode(multiResult.run1);
      final detourDistance = multiResult.totalDistanceInMeters - originalRes.totalDistanceInMeters;
      
      return {
        'encoded_polyline': encodedPolyline,
        'detour_meters': detourDistance,
        'waypoints': allWaypoints,
      };
    }
    return null;
  }

  Future<Map<String, dynamic>?> generateTripRoutePolyline(Map<String, dynamic> trip) async {
    String actualStartHash = trip['start_geohash'] as String? ?? '';
    String actualEndHash = trip['end_geohash'] as String? ?? '';

    // Backward compatibility fallback for older trips
    if (actualEndHash.isEmpty) {
      final campus = trip['campus'] as String?;
      final isToCampus = trip['trip_type'] == 'to_campus';
      if (campus != null) {
        final campusObj = CampusRepository.allCampuses.firstWhere((c) => c.id == campus, orElse: () => CampusRepository.allCampuses.first);
        final cHash = campusObj.geohashes.first;
        if (isToCampus) {
          actualEndHash = cHash;
        } else {
          actualStartHash = cHash;
          actualEndHash = trip['start_geohash'] as String? ?? '';
        }
      } else {
         return null; // Cannot determine destination
      }
    }

    if (actualStartHash.isEmpty || actualEndHash.isEmpty) return null;

    // Module 6: Multi-stop route for connected trips
    if (['connected'].contains(trip['status'])) {
      try {
        final geoHasher = GeoHasher();
        final rolesOffered = (trip['roles_offered'] as List<dynamic>?)?.cast<String>() ?? [];
        final isDriver = rolesOffered.contains('Driver') || rolesOffered.contains('Rider');
        final hitchhikerHashes = <String>[];

        if (isDriver) {
          final acceptedIds = (trip['accepted_hitchhikers'] as List<dynamic>?)?.cast<String>() ?? [];
          for (final hId in acceptedIds) {
            final hDoc = await FirebaseFirestore.instance.collection('trips').doc(hId).get();
            final hData = hDoc.data();
            if (hData != null) {
              final hHash = hData['trip_type'] == 'from_campus'
                  ? hData['end_geohash'] as String?
                  : hData['start_geohash'] as String?;
              if (hHash != null) hitchhikerHashes.add(hHash);
            }
          }
        } else {
          // Hitchhiker viewing the driver's route
          final dId = trip['driver_id'] as String?;
          if (dId != null) {
            final dDoc = await FirebaseFirestore.instance.collection('trips').doc(dId).get();
            final dData = dDoc.data();
            if (dData != null) {
              actualStartHash = dData['start_geohash'] as String? ?? actualStartHash;
              actualEndHash = dData['end_geohash'] as String? ?? actualEndHash;
              
              final hitchhikerDetour = trip['trip_type'] == 'from_campus'
                  ? trip['end_geohash'] as String?
                  : trip['start_geohash'] as String?;
              hitchhikerHashes.add(hitchhikerDetour ?? '');

              final otherAccepted = (dData['accepted_hitchhikers'] as List<dynamic>?)?.cast<String>() ?? [];
              for (final ohId in otherAccepted) {
                if (ohId != trip['id']) {
                  final ohDoc = await FirebaseFirestore.instance.collection('trips').doc(ohId).get();
                  final ohData = ohDoc.data();
                  if (ohData != null) {
                    final ohHash = ohData['trip_type'] == 'from_campus'
                        ? ohData['end_geohash'] as String?
                        : ohData['start_geohash'] as String?;
                    if (ohHash != null && !hitchhikerHashes.contains(ohHash)) {
                      hitchhikerHashes.add(ohHash);
                    }
                  }
                }
              }
            }
          }
        }

        String targetCampusId = trip['arrival_campus'] as String? ?? trip['campus'] as String? ?? trip['departure_campus'] as String? ?? '';
        final geoJsonPath = getGeoJsonPathForCampus(targetCampusId);
        final geojsonString = await rootBundle.loadString(geoJsonPath);
        
        final dCoords = geoHasher.decode(actualStartHash);
        final cCoords = geoHasher.decode(actualEndHash);
        
        final point1 = LatLng(dCoords[1], dCoords[0]);
        final point3 = LatLng(cCoords[1], cCoords[0]);

        final List<LatLng> waypoints = [];
        for (final hHash in hitchhikerHashes) {
          if (hHash.isNotEmpty) {
            final coords = geoHasher.decode(hHash);
            waypoints.add(LatLng(coords[1], coords[0]));
          }
        }

        final rp = ReceivePort();
        await Isolate.spawn(
          aStarIsolateMain,
          AStarIsolateData(rp.sendPort, geojsonString, point1, point3, waypoints: waypoints),
        );
        final AStarResult? result = await rp.first;
        rp.close();

        if (result != null && result.run1.isNotEmpty) {
          return {
            'encoded_polyline': PolylineEncoder.encode(result.run1),
            'waypoints': waypoints,
          };
        }
      } catch (e) {
        print("Error connected routing: \$e");
        // Fallback to simple route below
      }
    }

    // Simple route fallback
    String targetCampusId = trip['arrival_campus'] as String? ?? trip['campus'] as String? ?? trip['departure_campus'] as String? ?? '';
    final geoJsonPath = getGeoJsonPathForCampus(targetCampusId);
    final geojsonString = await rootBundle.loadString(geoJsonPath);
    final geoHasher = GeoHasher();
    
    final startCoords = geoHasher.decode(actualStartHash);
    final endCoords = geoHasher.decode(actualEndHash);
    
    final point1 = LatLng(startCoords[1], startCoords[0]);
    final point2 = LatLng(endCoords[1], endCoords[0]);

    final rp = ReceivePort();
    await Isolate.spawn(
      aStarIsolateMain,
      AStarIsolateData(rp.sendPort, geojsonString, point1, point2),
    );
    final AStarResult? result = await rp.first;
    rp.close();

    if (result != null && result.run1.isNotEmpty) {
      return {'encoded_polyline': PolylineEncoder.encode(result.run1)};
    }
    return null;
  }

  // Module 5 & 4: providerAcceptRoute moves hitchhiker from pending to accepted and saves detour
  Future<void> providerAcceptRoute(String driverTripId, String hitchhikerTripId, String encodedPolyline, double detourMeters) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      // Get hitchhiker uid to add to participants
      final hDoc = await FirebaseFirestore.instance.collection('trips').doc(hitchhikerTripId).get();
      final hitchhikerUid = hDoc.data()?['uid'] as String?;

      final batch = FirebaseFirestore.instance.batch();
      
      final hRef = FirebaseFirestore.instance.collection('trips').doc(hitchhikerTripId);
      final dRef = FirebaseFirestore.instance.collection('trips').doc(driverTripId);

      // Module 4: Time adoption & decluttering - hide original hitchhiker trip
      batch.update(hRef, {'status': 'hidden_adopted'});
      
      final driverUpdate = <String, dynamic>{
        'pending_requests': FieldValue.arrayRemove([hitchhikerTripId]),
        'accepted_hitchhikers': FieldValue.arrayUnion([hitchhikerTripId]),
        'encoded_polyline': encodedPolyline,
        'detour_meters': detourMeters,
        'status': 'connected',
      };
      if (hitchhikerUid != null) {
        driverUpdate['participants'] = FieldValue.arrayUnion([hitchhikerUid]);
      }
      batch.update(dRef, driverUpdate);
      
      await batch.commit();
      ref.invalidate(tripsProvider);
    });
  }

  Future<void> deleteTrip(String tripId) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await FirebaseFirestore.instance.collection('trips').doc(tripId).delete();
      ref.invalidate(tripsProvider);
    });
  }
}

final tripActionProvider = AsyncNotifierProvider<TripActionNotifier, void>(TripActionNotifier.new);
