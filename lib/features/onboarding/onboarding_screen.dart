import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:dart_geohash/dart_geohash.dart';
import 'onboarding_provider.dart';
import '../schedule/map_picker_screen.dart';
import '../../core/repositories/campus_repository.dart';
import '../../core/l10n/locale_provider.dart';

class OnboardingScreen extends ConsumerStatefulWidget {
  const OnboardingScreen({super.key});

  @override
  ConsumerState<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends ConsumerState<OnboardingScreen> {
  final _usernameController = TextEditingController();
  final _socialHandleController = TextEditingController();

  // Module 1: Flexible Profile Scheduling
  final Set<String> _selectedRoles = {};
  LatLng? _homeLocation;
  
  // day -> List of trips (maps)
  // Trip Map: { 'type': String, 'departure_campus': String?, 'arrival_campus': String?, 'time': TimeOfDay }
  final Map<String, List<Map<String, dynamic>>> _scheduleDays = {
    'Monday': [], 'Tuesday': [], 'Wednesday': [], 'Thursday': [], 'Friday': [], 'Saturday': [], 'Sunday': []
  };

  final List<String> _rolesList = ['Hitchhiker', 'Driver', 'Rider'];
  // Campuses fetched directly from repository

  final List<String> _daysList = [
    'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'
  ];
  final List<String> _tripTypes = ['to_campus', 'from_campus', 'campus_to_campus'];

  Future<void> _launchForm() async {
    final currentUser = FirebaseAuth.instance.currentUser;
    if (currentUser == null) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('User not logged in.')));
      return;
    }
    final uid = currentUser.uid;
    final Uri url = Uri.parse('https://docs.google.com/forms/d/e/1FAIpQLSeUROY0M9c_UhWKibOzGfpdOl8WDBRXSFqlv5QTReRFyR24Kg/viewform?usp=pp_url&entry.188822032=$uid');
    try {
      if (!await launchUrl(url, mode: LaunchMode.externalApplication)) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Could not open form: $url')));
      }
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error launching browser: $e')));
    }
  }

  Future<void> _pickHomeLocation() async {
    final result = await Navigator.of(context).push<LatLng>(
      MaterialPageRoute(builder: (_) => const MapPickerScreen(title: 'Home Location')),
    );
    if (result != null) {
      setState(() {
        _homeLocation = result;
      });
    }
  }

  Future<void> _addTripDialog(String day) async {
    String type = 'to_campus';
    String? depCampus;
    String? arrCampus;
    TimeOfDay time = TimeOfDay.now();

    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: Text('Add Trip - $day'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    DropdownButtonFormField<String>(
                      value: type,
                      decoration: const InputDecoration(labelText: 'Trip Type'),
                      items: _tripTypes.map((t) => DropdownMenuItem(value: t, child: Text(t.toLocalizedRole(ref.watch(localeProvider))))).toList(),
                      onChanged: (val) {
                        setDialogState(() {
                          type = val!;
                          depCampus = null;
                          arrCampus = null;
                        });
                      },
                    ),
                    const SizedBox(height: 16),
                    if (type == 'from_campus' || type == 'campus_to_campus')
                        DropdownButtonFormField<String>(
                          value: depCampus,
                          decoration: const InputDecoration(labelText: 'Departure Campus'),
                          items: CampusRepository.allCampuses.map((c) => DropdownMenuItem(value: c.id, child: Text(c.displayName))).toList(),
                          onChanged: (val) => setDialogState(() => depCampus = val),
                        ),
                    if (type == 'from_campus' || type == 'campus_to_campus')
                      const SizedBox(height: 16),
                    if (type == 'to_campus' || type == 'campus_to_campus')
                        DropdownButtonFormField<String>(
                          value: arrCampus,
                          decoration: const InputDecoration(labelText: 'Arrival Campus'),
                          items: CampusRepository.allCampuses.map((c) => DropdownMenuItem(value: c.id, child: Text(c.displayName))).toList(),
                          onChanged: (val) => setDialogState(() => arrCampus = val),
                        ),
                    const SizedBox(height: 16),
                    ListTile(
                      shape: RoundedRectangleBorder(side: BorderSide(color: Theme.of(context).colorScheme.outline)),
                      title: const Text('Schedule Time'),
                      subtitle: Text(time.format(context)),
                      trailing: const Icon(Icons.access_time),
                      onTap: () async {
                        final t = await showTimePicker(context: context, initialTime: time);
                        if (t != null) {
                          setDialogState(() => time = t);
                        }
                      },
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
                ElevatedButton(
                  onPressed: () {
                    if ((type == 'from_campus' || type == 'campus_to_campus') && depCampus == null) return;
                    if ((type == 'to_campus' || type == 'campus_to_campus') && arrCampus == null) return;
                    Navigator.pop(ctx, {
                      'type': type,
                      'departure_campus': depCampus,
                      'arrival_campus': arrCampus,
                      'time': time,
                    });
                  },
                  child: const Text('Add Trip'),
                )
              ],
            );
          }
        );
      }
    );

    if (result != null) {
      setState(() {
        _scheduleDays[day]!.add(result);
        // Sort trips by time
        _scheduleDays[day]!.sort((a, b) {
          final ta = a['time'] as TimeOfDay;
          final tb = b['time'] as TimeOfDay;
          return (ta.hour * 60 + ta.minute).compareTo(tb.hour * 60 + tb.minute);
        });
      });
    }
  }

  Future<void> _submit() async {
    final username = _usernameController.text.trim();
    final handle = _socialHandleController.text.trim();

    if (username.isEmpty || handle.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Please enter a username and handle')));
      return;
    }
    if (_selectedRoles.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Please select at least one role')));
      return;
    }
    if (_homeLocation == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Please pick your home location on the map')));
      return;
    }

    // Build the formatted schedule
    final Map<String, List<Map<String, String>>> formattedSchedule = {};
    for (final day in _scheduleDays.keys) {
      final trips = _scheduleDays[day]!;
      if (trips.isNotEmpty) {
        formattedSchedule[day] = trips.map((t) {
          final time = t['time'] as TimeOfDay;
          final timeStr = '${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}';
          return {
            'type': t['type'] as String,
            if (t['departure_campus'] != null) 'departure_campus': t['departure_campus'] as String,
            if (t['arrival_campus'] != null) 'arrival_campus': t['arrival_campus'] as String,
            'time': timeStr,
          };
        }).toList();
      }
    }

    try {
      showDialog(context: context, barrierDismissible: false, builder: (_) => const Center(child: CircularProgressIndicator()));

      final query = await FirebaseFirestore.instance.collection('users').where('username', isEqualTo: username).get();
      if (!mounted) return;
      
      if (query.docs.isNotEmpty) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Username already taken. Please choose another one.')));
        return;
      }

      await ref.read(userProfileNotifierProvider.notifier).submitProfile(
        username: username,
        socialHandle: handle,
        defaultRoles: _selectedRoles.toList(),
        homeGeohash: GeoHasher().encode(_homeLocation!.longitude, _homeLocation!.latitude, precision: 9),
        schedule: formattedSchedule,
      );
      
      if (mounted) Navigator.of(context).pop();
    } catch (e) {
      if (!mounted) return;
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final profileState = ref.watch(userProfileNotifierProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Profile Setup')),
      body: RefreshIndicator(
        onRefresh: () => ref.read(userProfileNotifierProvider.notifier).fetchProfile(),
        child: profileState.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, _) => ListView(
            padding: const EdgeInsets.all(24),
            children: [
              Text('Error: $error'),
              ElevatedButton(
                onPressed: () => ref.read(userProfileNotifierProvider.notifier).fetchProfile(),
                child: const Text('Retry'),
              )
            ],
          ),
          data: (profile) {
            if (profile != null) {
              final status = profile['status'] as String?;
              if (status == 'pending_verification') {
                return ListView(
                  padding: const EdgeInsets.all(24),
                  children: const [
                    Icon(Icons.hourglass_empty, size: 64, color: Colors.orange),
                    SizedBox(height: 16),
                    Text('Your profile is pending verification by the admin. Pull to refresh to check your status.', textAlign: TextAlign.center, style: TextStyle(fontSize: 18)),
                  ],
                );
              } else if (status == 'rejected') {
                return ListView(
                  padding: const EdgeInsets.all(24),
                  children: [
                    const Icon(Icons.error_outline, size: 64, color: Colors.red),
                    const SizedBox(height: 16),
                    Text('Your profile was rejected. Reason: ${profile['rejection_reason']}', textAlign: TextAlign.center, style: const TextStyle(fontSize: 18, color: Colors.red)),
                    const SizedBox(height: 24),
                    _buildForm(),
                  ],
                );
              } else if (status == 'verified') {
                return const Center(child: Text('Verified! Redirecting...'));
              }
            }
            return ListView(padding: const EdgeInsets.all(24), children: [_buildForm()]);
          },
        ),
      ),
    );
  }

  Widget _buildForm() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Text("1. Basic Info", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
        const SizedBox(height: 16),
        TextField(controller: _usernameController, decoration: const InputDecoration(labelText: 'Username', border: OutlineInputBorder())),
        const SizedBox(height: 16),
        TextField(controller: _socialHandleController, decoration: const InputDecoration(labelText: 'e.g. IG: @username or WA: 0812...', border: OutlineInputBorder())),
        const SizedBox(height: 32),

        const Text("2. Master Profile Settings", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
        const SizedBox(height: 16),
        const Text("Roles (Select all that apply)"),
        ..._rolesList.map((r) => CheckboxListTile(
          title: Text(r.toLocalizedRole(ref.watch(localeProvider))),
          value: _selectedRoles.contains(r),
          onChanged: (val) => setState(() {
            if (val == true) _selectedRoles.add(r);
            else _selectedRoles.remove(r);
          }),
        )),
        const SizedBox(height: 16),
        ListTile(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8), side: BorderSide(color: Theme.of(context).colorScheme.outline)),
          leading: const Icon(Icons.home),
          title: const Text('Home Location'),
          subtitle: Text(_homeLocation != null ? '${_homeLocation!.latitude.toStringAsFixed(4)}, ${_homeLocation!.longitude.toStringAsFixed(4)}' : 'Tap to pin on map'),
          onTap: _pickHomeLocation,
        ),
        const SizedBox(height: 32),

        const Text("3. Weekly Schedule", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
        const SizedBox(height: 8),
        const Text("Add multiple trips per day. Choose between 'to_campus', 'from_campus', or 'campus_to_campus'.", style: TextStyle(color: Colors.grey)),
        const SizedBox(height: 16),
        ..._daysList.map((day) {
          final trips = _scheduleDays[day]!;
          return Card(
            margin: const EdgeInsets.only(bottom: 12),
            child: Padding(
              padding: const EdgeInsets.all(12.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(day, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                      IconButton(
                        icon: const Icon(Icons.add_circle),
                        color: Theme.of(context).colorScheme.primary,
                        onPressed: () => _addTripDialog(day),
                      )
                    ],
                  ),
                  if (trips.isEmpty)
                    const Text('No trips scheduled', style: TextStyle(color: Colors.grey, fontStyle: FontStyle.italic)),
                  ...trips.asMap().entries.map((entry) {
                    final index = entry.key;
                    final t = entry.value;
                    final time = t['time'] as TimeOfDay;
                    String desc = t['type'];
                    if (t['type'] == 'to_campus') desc = 'To ${t['arrival_campus']}';
                    if (t['type'] == 'from_campus') desc = 'From ${t['departure_campus']}';
                    if (t['type'] == 'campus_to_campus') desc = '${t['departure_campus']} -> ${t['arrival_campus']}';
                    
                    return ListTile(
                      dense: true,
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.directions_car),
                      title: Text(desc),
                      subtitle: Text(time.format(context)),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete, color: Colors.red, size: 20),
                        onPressed: () {
                          setState(() {
                            _scheduleDays[day]!.removeAt(index);
                          });
                        },
                      ),
                    );
                  })
                ],
              ),
            ),
          );
        }),
        const SizedBox(height: 32),

        const Text("4. Verification", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
        const SizedBox(height: 12),
        ElevatedButton.icon(
          onPressed: _launchForm,
          icon: const Icon(Icons.open_in_browser),
          label: const Text('Step 1: Upload ID via Form'),
          style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 16), backgroundColor: Theme.of(context).colorScheme.primaryContainer),
        ),
        const SizedBox(height: 12),
        ElevatedButton(
          onPressed: _submit,
          style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 16), backgroundColor: Theme.of(context).colorScheme.primary, foregroundColor: Theme.of(context).colorScheme.onPrimary),
          child: const Text('Step 2: Save & Generate Trips'),
        ),
      ],
    );
  }
}
