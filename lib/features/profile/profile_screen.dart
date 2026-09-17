import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';
import 'package:go_router/go_router.dart';
import 'package:dart_geohash/dart_geohash.dart';
import '../schedule/map_picker_screen.dart';
import '../trips/trip_provider.dart';
import 'receipt_screen.dart';
import '../../core/repositories/campus_repository.dart';
import '../../core/l10n/locale_provider.dart';

class ProfileScreen extends ConsumerStatefulWidget {
  const ProfileScreen({super.key});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  final _usernameController = TextEditingController();
  final _socialHandleController = TextEditingController();
  String? _originalUsername;
  bool _isLoading = true;
  bool _isSaving = false;

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

  @override
  void initState() {
    super.initState();
    _loadProfileData();
  }

  Future<void> _loadProfileData() async {
    final user = FirebaseAuth.instance.currentUser;
    if (user != null) {
      final doc = await FirebaseFirestore.instance.collection('users').doc(user.uid).get();
      if (doc.exists) {
        final data = doc.data();
        if (data != null) {
          if (data['username'] != null) {
            _usernameController.text = data['username'];
            _originalUsername = data['username'];
          }
          if (data['social_handle'] != null) {
            _socialHandleController.text = data['social_handle'];
          }
          
          if (data['default_roles'] != null) {
            _selectedRoles.addAll((data['default_roles'] as List<dynamic>).cast<String>());
          }
          if (data['home_lat'] != null && data['home_lng'] != null) {
            _homeLocation = LatLng(data['home_lat'], data['home_lng']);
          }

          for (final day in _daysList) {
            final dayKey = 'schedule_${day.toLowerCase()}';
            if (data[dayKey] != null) {
              final tripsArray = data[dayKey] as List<dynamic>;
              for (final trip in tripsArray) {
                final tripMap = trip as Map<String, dynamic>;
                final timeStr = tripMap['time'] as String;
                final timeParts = timeStr.split(':');
                _scheduleDays[day]!.add({
                  'type': tripMap['type'],
                  'departure_campus': tripMap['departure_campus'],
                  'arrival_campus': tripMap['arrival_campus'],
                  'time': TimeOfDay(hour: int.parse(timeParts[0]), minute: int.parse(timeParts[1])),
                });
              }
            }
          }
        }
      }
    }
    setState(() {
      _isLoading = false;
    });
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
        _scheduleDays[day]!.sort((a, b) {
          final ta = a['time'] as TimeOfDay;
          final tb = b['time'] as TimeOfDay;
          return (ta.hour * 60 + ta.minute).compareTo(tb.hour * 60 + tb.minute);
        });
      });
    }
  }

  Future<void> _saveProfileData() async {
    final handle = _socialHandleController.text.trim();
    final username = _usernameController.text.trim();
    
    if (username.isEmpty || handle.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Username and handle cannot be empty')));
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

    setState(() { _isSaving = true; });

    try {
      if (username != _originalUsername) {
        final usernameQuery = await FirebaseFirestore.instance.collection('users').where('username', isEqualTo: username).get();
        if (usernameQuery.docs.isNotEmpty) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Username already taken. Please choose another one.')));
            setState(() { _isSaving = false; });
          }
          return;
        }
      }

      final user = FirebaseAuth.instance.currentUser;
      if (user != null) {
        final updateData = <String, dynamic>{
          'username': username,
          'social_handle': handle,
          'default_roles': _selectedRoles.toList(),
          'home_geohash': GeoHasher().encode(_homeLocation!.longitude, _homeLocation!.latitude, precision: 9),
        };

        for (final day in _daysList) {
          final dayKey = 'schedule_${day.toLowerCase()}';
          final trips = _scheduleDays[day]!;
          if (trips.isNotEmpty) {
            updateData[dayKey] = trips.map((t) {
              final time = t['time'] as TimeOfDay;
              final timeStr = '${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}';
              return {
                'type': t['type'] as String,
                if (t['departure_campus'] != null) 'departure_campus': t['departure_campus'] as String,
                if (t['arrival_campus'] != null) 'arrival_campus': t['arrival_campus'] as String,
                'time': timeStr,
              };
            }).toList();
          } else {
            updateData[dayKey] = null;
          }
        }

        await FirebaseFirestore.instance.collection('users').doc(user.uid).update(updateData);
        _originalUsername = username;
        
        // Auto-generate trips
        await ref.read(tripActionProvider.notifier).autoGenerateTrips(updateData);
        
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Profile updated and trips generated!')));
        }
      }
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error updating profile: $e')));
    } finally {
      if (mounted) setState(() { _isSaving = false; });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('My Profile')),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text('Basic Info', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 16),
                  TextField(controller: _usernameController, decoration: const InputDecoration(labelText: 'Username', border: OutlineInputBorder())),
                  const SizedBox(height: 16),
                  TextField(controller: _socialHandleController, decoration: const InputDecoration(labelText: 'Contact (IG/WA)', border: OutlineInputBorder())),
                  const SizedBox(height: 32),

                  const Text('Master Profile Settings', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
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

                  const Text('Weekly Schedule', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
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
                  
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: _isSaving ? null : _saveProfileData,
                    style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 16)),
                    child: _isSaving
                        ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                        : const Text('Save & Auto-Generate Trips'),
                  ),
                  
                  const SizedBox(height: 48),
                  const Divider(),
                  const SizedBox(height: 16),
                  
                  // Module 3: Tip Developer moved to bottom
                  _buildTipDeveloperCard(context),
                  
                  // Module 3: Log Out button nested directly beneath Tip card
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: () async {
                      await FirebaseAuth.instance.signOut();
                      if (context.mounted) context.go('/login');
                    },
                    icon: const Icon(Icons.logout),
                    label: const Text('Log Out'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red.shade100,
                      foregroundColor: Colors.red.shade900,
                    ),
                  ),
                  const SizedBox(height: 32),
                ],
              ),
            ),
    );
  }

  Widget _buildTipDeveloperCard(BuildContext context) {
    return Card(
      color: Theme.of(context).colorScheme.surfaceContainerHighest,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            const Icon(Icons.favorite, color: Colors.red, size: 40),
            const SizedBox(height: 16),
            const Text('Tip the Developer', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
            const SizedBox(height: 8),
            const Text('Bank Jago: 107301692693', style: TextStyle(fontSize: 16)),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: () async {
                await Clipboard.setData(const ClipboardData(text: '107301692693'));
                if (context.mounted) {
                  // Module 5: Show receipt screen
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const ReceiptScreen()),
                  );
                }
              },
              icon: const Icon(Icons.card_giftcard),
              label: const Text('Send a Tip!'),
            ),
            const SizedBox(height: 16),
            const Text('Support our server costs!', style: TextStyle(fontStyle: FontStyle.italic)),
          ],
        ),
      ),
    );
  }
}
