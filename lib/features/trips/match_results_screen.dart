import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'trip_provider.dart';
import '../../core/l10n/locale_provider.dart';

class MatchResultsScreen extends ConsumerStatefulWidget {
  final List<Map<String, dynamic>> matchedTrips;
  final String hitchhikerTripId;

  const MatchResultsScreen({
    super.key,
    required this.matchedTrips,
    required this.hitchhikerTripId,
  });

  @override
  ConsumerState<MatchResultsScreen> createState() => _MatchResultsScreenState();
}

class _MatchResultsScreenState extends ConsumerState<MatchResultsScreen> {
  String _selectedRole = 'All'; // 'All', 'Driver', 'Rider'

  @override
  Widget build(BuildContext context) {
    final filteredTrips = widget.matchedTrips.where((match) {
      if (_selectedRole == 'All') return true;
      final roles = (match['roles_offered'] as List<dynamic>?)?.cast<String>() ?? [];
      return roles.contains(_selectedRole);
    }).toList();

    return Scaffold(
      appBar: AppBar(title: const Text('Matched Providers')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: SegmentedButton<String>(
              segments: [
                const ButtonSegment(value: 'All', label: Text('All')),
                ButtonSegment(value: 'Driver', label: Text('Driver'.toLocalizedRole(ref.watch(localeProvider)))),
                ButtonSegment(value: 'Rider', label: Text('Rider'.toLocalizedRole(ref.watch(localeProvider)))),
              ],
              selected: {_selectedRole},
              onSelectionChanged: (Set<String> newSelection) {
                setState(() {
                  _selectedRole = newSelection.first;
                });
              },
            ),
          ),
          Expanded(
            child: filteredTrips.isEmpty
                ? const Center(child: Text('No providers found matching your schedule.'))
                : ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: filteredTrips.length,
                    itemBuilder: (context, index) {
                      final match = filteredTrips[index];
                      final driverTripId = match['id'] as String;
                      final scheduleTime = match['schedule_time'] as String;
                      final driverUid = match['uid'] as String?;
                      final similarityScore = match['similarity_score'] as int? ?? 0;
                      final rolesOffered = (match['roles_offered'] as List<dynamic>?)?.cast<String>().map((r) => r.toLocalizedRole(ref.watch(localeProvider))).join(', ') ?? 'Unknown';

                      return Card(
                        margin: const EdgeInsets.only(bottom: 16),
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text('Provider Match', style: Theme.of(context).textTheme.titleLarge),
                                  Chip(
                                    label: Text('Similarity: $similarityScore', style: const TextStyle(fontWeight: FontWeight.bold)),
                                    backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                                  ),
                                ],
                              ),
                              Text('Roles: $rolesOffered', style: const TextStyle(fontWeight: FontWeight.bold)),
                              if (match['departure_campus'] != null) Text('Departure: ${match['departure_campus']}'),
                              if (match['arrival_campus'] != null) Text('Arrival: ${match['arrival_campus']}'),
                              Text('Day: ${match['day_of_week']}'),
                              Text('Time: $scheduleTime', style: const TextStyle(fontWeight: FontWeight.bold)),

                              if (driverUid != null)
                                FutureBuilder<DocumentSnapshot>(
                                  future: FirebaseFirestore.instance.collection('users').doc(driverUid).get(),
                                  builder: (context, userSnapshot) {
                                    if (!userSnapshot.hasData) {
                                      return const Padding(
                                        padding: EdgeInsets.only(top: 8),
                                        child: SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2)),
                                      );
                                    }
                                    final userData = userSnapshot.data?.data() as Map<String, dynamic>?;
                                    if (userData == null) return const SizedBox.shrink();
                                    
                                    return Container(
                                      margin: const EdgeInsets.only(top: 12),
                                      padding: const EdgeInsets.all(12),
                                      decoration: BoxDecoration(
                                        color: Theme.of(context).colorScheme.surfaceContainerHighest,
                                        borderRadius: BorderRadius.circular(8),
                                      ),
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text('${userData['username'] ?? 'Unknown'}\'s Schedule', style: const TextStyle(fontWeight: FontWeight.bold)),
                                          const SizedBox(height: 8),
                                          ...['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'].map((day) {
                                            final dayKey = 'schedule_${day.toLowerCase()}';
                                            final trips = userData[dayKey] as List<dynamic>? ?? [];
                                            if (trips.isEmpty) return const SizedBox.shrink();
                                            return Padding(
                                              padding: const EdgeInsets.only(bottom: 4.0),
                                              child: Column(
                                                crossAxisAlignment: CrossAxisAlignment.start,
                                                children: [
                                                  Text(day, style: const TextStyle(fontWeight: FontWeight.w600)),
                                                  ...trips.map((t) {
                                                    final tripMap = t as Map<String, dynamic>;
                                                    String desc = tripMap['type'];
                                                    final lang = ref.watch(localeProvider);
                                                    if (tripMap['type'] == 'to_campus') desc = '${'to_campus'.toLocalizedRole(lang)} ${tripMap['arrival_campus']}';
                                                    if (tripMap['type'] == 'from_campus') desc = '${'from_campus'.toLocalizedRole(lang)} ${tripMap['departure_campus']}';
                                                    if (tripMap['type'] == 'campus_to_campus') desc = '${tripMap['departure_campus']} -> ${tripMap['arrival_campus']}';
                                                    return Text('- $desc at ${tripMap['time']}');
                                                  }),
                                                ],
                                              ),
                                            );
                                          })
                                        ],
                                      ),
                                    );
                                  },
                                ),

                              const SizedBox(height: 16),
                              ElevatedButton.icon(
                                onPressed: () async {
                                  try {
                                    showDialog(
                                      context: context,
                                      barrierDismissible: false,
                                      builder: (_) => const Center(child: CircularProgressIndicator())
                                    );
                                    
                                    await ref.read(tripActionProvider.notifier).requestSeat(widget.hitchhikerTripId, driverTripId);
                                    
                                    if (context.mounted) {
                                      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Request Sent!')));
                                      Navigator.of(context).pop(); // dismiss loading
                                      Navigator.of(context).pop(); // dismiss screen
                                    }
                                  } catch (e) {
                                    if (context.mounted) {
                                      Navigator.of(context).pop();
                                      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
                                    }
                                  }
                                },
                                icon: const Icon(Icons.hail),
                                label: const Text('Request Seat'),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
