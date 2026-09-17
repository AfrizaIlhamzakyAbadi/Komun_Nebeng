import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:latlong2/latlong.dart';
import 'package:firebase_auth/firebase_auth.dart';

import 'trip_provider.dart';
import 'route_map_screen.dart';
import 'match_results_screen.dart';
import '../../core/l10n/locale_provider.dart';

class TripScreen extends ConsumerWidget {
  const TripScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tripsAsync = ref.watch(tripsProvider);

    // Module 3: UI Simplification - Removed Tabs
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Trips'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person),
            onPressed: () {
              context.push('/profile');
            },
            tooltip: 'Profile',
          ),
          // Removed Logout button from here
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(tripsProvider);
        },
        child: tripsAsync.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(child: Text('Error: $e')),
          data: (trips) {
            if (trips.isEmpty) {
              return ListView(
                children: const [
                  SizedBox(height: 100),
                  Center(
                    child: Text(
                      'No trips found.\nYour trips are auto-generated from your Profile schedule.',
                      textAlign: TextAlign.center,
                    ),
                  ),
                ],
              );
            }

            return ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: trips.length,
              itemBuilder: (context, index) {
                final trip = trips[index];
                return _buildTripCard(context, ref, trip);
              },
            );
          },
        ),
      ),
    );
  }

  Widget _buildTripCard(BuildContext context, WidgetRef ref, Map<String, dynamic> trip) {
    final status = trip['status'] as String?;
    final tripId = trip['id'] as String;
    
    final currentUid = FirebaseAuth.instance.currentUser?.uid;
    final isTripOwner = trip['uid'] == currentUid;
    
    final rolesOffered = (trip['roles_offered'] as List<dynamic>?)?.cast<String>() ?? [];
    final isHitchhiker = rolesOffered.contains('Hitchhiker');
    final isDriverOrRider = rolesOffered.contains('Driver') || rolesOffered.contains('Rider');
    
    // Backward compat
    final oldOtherTripId = isHitchhiker ? trip['driver_id'] as String? : trip['hitchhiker_id'] as String?;
    final pendingRequests = (trip['pending_requests'] as List<dynamic>?)?.cast<String>() ?? [];
    final acceptedHitchhikers = (trip['accepted_hitchhikers'] as List<dynamic>?)?.cast<String>() ?? [];

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Builder(builder: (context) {
              String tripTitle = 'Unknown Trip';
              final lang = ref.watch(localeProvider);
              if (trip['trip_type'] == 'to_campus') {
                tripTitle = '${'to_campus'.toLocalizedRole(lang)} ${trip['arrival_campus'] ?? trip['campus']}';
              } else if (trip['trip_type'] == 'from_campus') {
                tripTitle = '${'from_campus'.toLocalizedRole(lang)} ${trip['departure_campus'] ?? trip['campus']}';
              } else if (trip['trip_type'] == 'campus_to_campus') {
                tripTitle = '${trip['departure_campus']} -> ${trip['arrival_campus']}';
              }
              return Text('Trip: $tripTitle', style: Theme.of(context).textTheme.titleLarge);
            }),
            Text('${trip['day_of_week'] ?? ''} at ${trip['schedule_time'] ?? ''}'),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Status: ${acceptedHitchhikers.isNotEmpty ? 'CONNECTED' : status?.toUpperCase()}'),
                // Auto-generated trips can still be deleted if user wants to cancel one off
                IconButton(
                  icon: const Icon(Icons.delete, color: Colors.red),
                  onPressed: () async {
                    final confirm = await showDialog<bool>(
                      context: context,
                      builder: (ctx) => AlertDialog(
                        title: const Text('Delete Trip?'),
                        content: const Text('Are you sure you want to delete this trip request?'),
                        actions: [
                          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancel')),
                          TextButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Delete', style: TextStyle(color: Colors.red))),
                        ]
                      )
                    );
                    if (confirm == true) {
                      ref.read(tripActionProvider.notifier).deleteTrip(tripId);
                    }
                  },
                )
              ]
            ),
            if (trip['detour_meters'] != null)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text('Detour: ${(trip['detour_meters'] as double).toStringAsFixed(0)} meters', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.blue)),
              ),
            const SizedBox(height: 16),

            // View Route button
            ElevatedButton.icon(
              onPressed: () async {
                try {
                  showDialog(
                    context: context,
                    barrierDismissible: false,
                    builder: (_) => const Center(child: CircularProgressIndicator())
                  );

                  final routeData = await ref.read(tripActionProvider.notifier).generateTripRoutePolyline(trip);

                  if (context.mounted) {
                    Navigator.of(context).pop(); // dismiss loading
                    if (routeData != null) {
                      final polyline = routeData['encoded_polyline'] as String;
                      final waypointsList = (routeData['waypoints'] as List<LatLng>?) ?? [];
                      // Backward compat
                      if (waypointsList.isEmpty) {
                        final wLat = routeData['waypoint_lat'] as double?;
                        final wLng = routeData['waypoint_lng'] as double?;
                        if (wLat != null && wLng != null) {
                          waypointsList.add(LatLng(wLat, wLng));
                        }
                      }

                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => RouteMapScreen(
                            encodedPolyline: polyline,
                            waypoints: waypointsList,
                            title: 'Route Preview',
                          ),
                        ),
                      );
                    } else {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Could not generate route locally.'))
                      );
                    }
                  }
                } catch (e) {
                  if (context.mounted) {
                    Navigator.of(context).pop();
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
                  }
                }
              },
              icon: const Icon(Icons.map),
              label: const Text('View Route'),
            ),

            // Find Drivers button — Hitchhiker searching
            if (status == 'searching' && isHitchhiker)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: ElevatedButton.icon(
                  onPressed: () async {
                    try {
                      showDialog(
                        context: context, 
                        barrierDismissible: false,
                        builder: (_) => const Center(child: CircularProgressIndicator())
                      );
                      
                      final matches = await ref.read(tripActionProvider.notifier).findMatchesForHitchhiker(trip);
                      
                      if (context.mounted) {
                        Navigator.of(context).pop(); // dismiss loading
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => MatchResultsScreen(
                              matchedTrips: matches,
                              hitchhikerTripId: tripId,
                            ),
                          ),
                        );
                      }
                    } catch (e) {
                      if (context.mounted) {
                        Navigator.of(context).pop();
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
                      }
                    }
                  },
                  icon: const Icon(Icons.search),
                  label: const Text('Find Providers'),
                ),
              ),
            const SizedBox(height: 8),
            
            // --- Backward compat: old single-match details ---
            // State Machine Cleanup: Removed completed, confirmed, in_progress
            if (oldOtherTripId != null && ['request_pending', 'connected'].contains(status))
              _buildOldMatchDetails(context, ref, tripId, oldOtherTripId, status),

            // --- Multi-Request — Pending requests list for Driver ---
            if (isTripOwner && isDriverOrRider && pendingRequests.isNotEmpty)
              _buildPendingRequestsList(context, ref, trip, tripId, pendingRequests),

            // --- Accepted hitchhikers list for Driver ---
            if (isTripOwner && isDriverOrRider && acceptedHitchhikers.isNotEmpty)
              _buildAcceptedHitchhikersList(context, ref, tripId, acceptedHitchhikers),

            // Cancel request — Hitchhiker side
            if (status == 'request_pending' && isHitchhiker && oldOtherTripId != null)
              ElevatedButton(
                onPressed: () => ref.read(tripActionProvider.notifier).cancelRequest(tripId, oldOtherTripId),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.red, foregroundColor: Colors.white),
                child: const Text('Cancel Request'),
              ),

            // Connected status display
            if (status == 'connected' || (!isTripOwner && acceptedHitchhikers.isNotEmpty)) ...[
              const SizedBox(height: 8),
              const Text('Matched!', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.green)),
              const SizedBox(height: 12),
              
              if (!isTripOwner) 
                _buildConnectedMatchProfile(context, ref, tripId), // Hitchhiker sees the Driver's profile
              if (isTripOwner && isHitchhiker && oldOtherTripId != null)
                _buildConnectedMatchProfile(context, ref, oldOtherTripId), // Old fallback
              if (isTripOwner && isDriverOrRider && acceptedHitchhikers.isEmpty && oldOtherTripId != null)
                _buildConnectedMatchProfile(context, ref, oldOtherTripId), // Old fallback
            ],
          ],
        ),
      ),
    );
  }

  /// Backward-compat: Old single-match details (FutureBuilder)
  Widget _buildOldMatchDetails(BuildContext context, WidgetRef ref, String tripId, String otherTripId, String? status) {
    return FutureBuilder<DocumentSnapshot>(
      future: FirebaseFirestore.instance.collection('trips').doc(otherTripId).get(),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return const SizedBox.shrink();
        final otherData = snapshot.data!.data() as Map<String, dynamic>?;
        if (otherData == null) return const SizedBox.shrink();
        final rolesList = (otherData['roles_offered'] as List<dynamic>?)?.cast<String>() ?? [];
        final otherRole = rolesList.isNotEmpty 
            ? rolesList.map((r) => r.toLocalizedRole(ref.watch(localeProvider))).join('/') 
            : 'Match';
        final matchedUid = otherData['uid'] as String?;
        
        return Container(
          margin: const EdgeInsets.only(top: 8, bottom: 16),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.primaryContainer.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('$otherRole Details', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              const SizedBox(height: 4),
              Text('Time: ${otherData['schedule_time'] ?? 'Unknown'}'),
              Text('Day: ${otherData['day_of_week'] ?? 'Unknown'}'),
              if (status == 'connected' && matchedUid != null) ...[
                const SizedBox(height: 8),
                FutureBuilder<DocumentSnapshot>(
                  future: FirebaseFirestore.instance.collection('users').doc(matchedUid).get(),
                  builder: (context, userSnapshot) {
                    if (!userSnapshot.hasData) return const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2));
                    final userData = userSnapshot.data?.data() as Map<String, dynamic>?;
                    final handle = userData?['social_handle'] ?? 'Unknown';
                    return Text('Contact your match: $handle', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.green));
                  }
                )
              ]
            ],
          ),
        );
      },
    );
  }

  /// Display list of pending requests for driver
  Widget _buildPendingRequestsList(BuildContext context, WidgetRef ref, Map<String, dynamic> driverTrip, String driverTripId, List<String> pendingRequests) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Divider(),
        Text('Pending Requests (${pendingRequests.length})', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        const SizedBox(height: 8),
        ...pendingRequests.map((hitchhikerTripId) => _buildPendingRequestCard(context, ref, driverTrip, driverTripId, hitchhikerTripId)),
      ],
    );
  }

  /// Single pending request card with hitchhiker profile and View Request button
  Widget _buildPendingRequestCard(BuildContext context, WidgetRef ref, Map<String, dynamic> driverTrip, String driverTripId, String hitchhikerTripId) {
    return FutureBuilder<DocumentSnapshot>(
      future: FirebaseFirestore.instance.collection('trips').doc(hitchhikerTripId).get(),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return const Padding(padding: EdgeInsets.all(8), child: CircularProgressIndicator());
        final hData = snapshot.data?.data() as Map<String, dynamic>?;
        if (hData == null) return const Text('Request data not found');
        
        final hUid = hData['uid'] as String?;
        
        return Card(
          margin: const EdgeInsets.only(bottom: 8),
          color: Theme.of(context).colorScheme.surfaceContainerHighest,
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text('Time: ${hData['schedule_time'] ?? 'Unknown'}'),
                Text('Day: ${hData['day_of_week'] ?? 'Unknown'}'),
                if (hUid != null)
                  FutureBuilder<DocumentSnapshot>(
                    future: FirebaseFirestore.instance.collection('users').doc(hUid).get(),
                    builder: (context, userSnapshot) {
                      if (!userSnapshot.hasData) return const SizedBox.shrink();
                      final userData = userSnapshot.data?.data() as Map<String, dynamic>?;
                      if (userData == null) return const SizedBox.shrink();
                      return Padding(
                        padding: const EdgeInsets.only(top: 4),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Username: ${userData['username'] ?? 'Unknown'}', style: const TextStyle(fontWeight: FontWeight.bold)),
                            const SizedBox(height: 8),
                            ..._buildScheduleWidgets(userData, ref.watch(localeProvider)),
                          ],
                        ),
                      );
                    },
                  ),
                const SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton.icon(
                      onPressed: () {
                        ref.read(tripActionProvider.notifier).cancelRequest(hitchhikerTripId, driverTripId);
                      },
                      icon: const Icon(Icons.close, color: Colors.red),
                      label: const Text('Reject', style: TextStyle(color: Colors.red)),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton.icon(
                      onPressed: () async {
                        try {
                          showDialog(
                            context: context, 
                            barrierDismissible: false,
                            builder: (_) => const Center(child: CircularProgressIndicator())
                          );
                          
                          final driverStartHash = driverTrip['start_geohash'] as String;
                          final driverEndHash = driverTrip['end_geohash'] as String?;
                          
                          final hitchhikerHash = hData['trip_type'] == 'from_campus'
                              ? hData['end_geohash'] as String
                              : hData['start_geohash'] as String;

                          final acceptedIds = (driverTrip['accepted_hitchhikers'] as List<dynamic>?)?.cast<String>() ?? [];
                          final List<String> existingWaypointHashes = [];
                          for (final ahId in acceptedIds) {
                            final ahDoc = await FirebaseFirestore.instance.collection('trips').doc(ahId).get();
                            final ahData = ahDoc.data();
                            if (ahData != null) {
                              final ahHash = ahData['trip_type'] == 'from_campus'
                                  ? ahData['end_geohash'] as String?
                                  : ahData['start_geohash'] as String?;
                              if (ahHash != null) existingWaypointHashes.add(ahHash);
                            }
                          }

                          if (driverEndHash == null) {
                             throw Exception('Old trip document format detected. Please regenerate trips.');
                          }

                          final preview = await ref.read(tripActionProvider.notifier).previewWaypointRoute(
                            driverStartHash, 
                            driverEndHash,
                            hitchhikerHash, 
                            existingWaypointHashes: existingWaypointHashes,
                          );
                          
                          if (context.mounted) {
                            Navigator.of(context).pop(); // dismiss loading
                            if (preview != null) {
                              final detour = preview['detour_meters'] as double;
                              final encodedRoute = preview['encoded_polyline'] as String;
                              final waypointsList = (preview['waypoints'] as List<LatLng>?) ?? [];
                              
                              String detourText = detour < 1000 
                                  ? "+ ${detour.toStringAsFixed(0)}m detour"
                                  : "+ ${(detour / 1000).toStringAsFixed(1)}km detour";
                                  
                              showDialog(
                                context: context,
                                builder: (ctx) => AlertDialog(
                                  title: const Text('Route Preview'),
                                  content: Column(
                                    mainAxisSize: MainAxisSize.min,
                                    children: [
                                      Text('New route calculated.\n$detourText', style: const TextStyle(fontWeight: FontWeight.bold)),
                                      const SizedBox(height: 16),
                                      SizedBox(
                                        height: 200,
                                        width: double.maxFinite,
                                        child: RouteMapScreen(
                                          encodedPolyline: encodedRoute, 
                                          title: 'Preview',
                                          waypoints: waypointsList,
                                        ),
                                      )
                                    ],
                                  ),
                                  actions: [
                                    TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Cancel')),
                                    ElevatedButton(
                                      onPressed: () {
                                        Navigator.of(ctx).pop();
                                        ref.read(tripActionProvider.notifier).providerAcceptRoute(driverTripId, hitchhikerTripId, encodedRoute, detour);
                                      },
                                      child: const Text('Accept Route'),
                                    ),
                                  ],
                                ),
                              );
                            }
                          }
                        } catch (e) {
                          if (context.mounted) {
                            Navigator.of(context).pop();
                            ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
                          }
                        }
                      },
                      icon: const Icon(Icons.remove_red_eye),
                      label: const Text('View Request'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  /// Display accepted hitchhikers for driver
  Widget _buildAcceptedHitchhikersList(BuildContext context, WidgetRef ref, String driverTripId, List<String> acceptedHitchhikers) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Divider(),
        Text('Accepted Hitchhikers (${acceptedHitchhikers.length})', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.green)),
        const SizedBox(height: 8),
        ...acceptedHitchhikers.map((hTripId) => FutureBuilder<DocumentSnapshot>(
          future: FirebaseFirestore.instance.collection('trips').doc(hTripId).get(),
          builder: (context, snapshot) {
            if (!snapshot.hasData) return const SizedBox.shrink();
            final hData = snapshot.data?.data() as Map<String, dynamic>?;
            if (hData == null) return const Text('Trip data not found');
            final hUid = hData['uid'] as String?;
            
            return Card(
              margin: const EdgeInsets.only(bottom: 8),
              color: Colors.green.withValues(alpha: 0.15),
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Accepted', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.green)),
                    Text('Time: ${hData['schedule_time'] ?? 'Unknown'}'),
                    Text('Day: ${hData['day_of_week'] ?? 'Unknown'}'),
                    if (hUid != null)
                      FutureBuilder<DocumentSnapshot>(
                        future: FirebaseFirestore.instance.collection('users').doc(hUid).get(),
                        builder: (context, userSnapshot) {
                          if (!userSnapshot.hasData) return const SizedBox.shrink();
                          final userData = userSnapshot.data?.data() as Map<String, dynamic>?;
                          if (userData == null) return const SizedBox.shrink();
                          return Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Username: ${userData['username'] ?? 'Unknown'}'),
                              Text('Contact: ${userData['social_handle'] ?? 'Unknown'}', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.green)),
                              ..._buildScheduleWidgets(userData, ref.watch(localeProvider)),
                            ],
                          );
                        },
                      ),
                  ],
                ),
              ),
            );
          },
        )),
      ],
    );
  }

  /// Connected match profile card
  Widget _buildConnectedMatchProfile(BuildContext context, WidgetRef ref, String otherTripId) {
    return FutureBuilder<DocumentSnapshot>(
      future: FirebaseFirestore.instance.collection('trips').doc(otherTripId).get(),
      builder: (context, tripSnapshot) {
        if (!tripSnapshot.hasData) return const Center(child: CircularProgressIndicator());
        final otherTrip = tripSnapshot.data!.data() as Map<String, dynamic>?;
        final matchedUid = otherTrip?['uid'] as String?;
        if (matchedUid == null) return const SizedBox.shrink();
        
        return FutureBuilder<DocumentSnapshot>(
          future: FirebaseFirestore.instance.collection('users').doc(matchedUid).get(),
          builder: (context, userSnapshot) {
            if (!userSnapshot.hasData) return const Center(child: CircularProgressIndicator());
            final userData = userSnapshot.data?.data() as Map<String, dynamic>?;
            final username = userData?['username'] ?? 'Unknown';
            final handle = userData?['social_handle'] ?? 'Unknown';
            
            return Card(
              color: Colors.green.withValues(alpha: 0.15),
              elevation: 2,
              margin: const EdgeInsets.only(bottom: 8),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Match Profile', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.green)),
                    const Divider(),
                    Text('Username: $username', style: const TextStyle(fontSize: 16)),
                    const SizedBox(height: 4),
                    Text('Contact: $handle', style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    ..._buildScheduleWidgets(userData ?? {}, ref.watch(localeProvider)),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  List<Widget> _buildScheduleWidgets(Map<String, dynamic> userData, String langCode) {
    final List<Widget> scheduleWidgets = [];
    final days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    for (final day in days) {
      final key = 'schedule_${day.toLowerCase()}';
      if (userData[key] != null) {
        try {
          final tripsForDay = userData[key] as List<dynamic>;
          for (final t in tripsForDay) {
            final tripMap = t as Map<String, dynamic>;
            final type = tripMap['type'];
            final time = tripMap['time'];
            String dest = '';
            if (type == 'to_campus') dest = '${'to_campus'.toLocalizedRole(langCode)} ${tripMap['arrival_campus']}';
            if (type == 'from_campus') dest = '${'from_campus'.toLocalizedRole(langCode)} ${tripMap['departure_campus']}';
            if (type == 'campus_to_campus') dest = '${tripMap['departure_campus']} -> ${tripMap['arrival_campus']}';
            scheduleWidgets.add(Text('$day: $dest at $time', style: const TextStyle(fontSize: 12)));
          }
        } catch (e) {
          // ignore parsing errors
        }
      }
    }
    if (scheduleWidgets.isNotEmpty) {
      return [
        const Text('Weekly Schedule:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12, decoration: TextDecoration.underline)),
        ...scheduleWidgets,
      ];
    }
    return [];
  }
}
