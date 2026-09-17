import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'map_picker_screen.dart';
import '../trips/trip_provider.dart';
import '../../core/repositories/campus_repository.dart';
import '../../core/l10n/locale_provider.dart';
class ScheduleBuilderScreen extends ConsumerStatefulWidget {
  const ScheduleBuilderScreen({super.key});

  @override
  ConsumerState<ScheduleBuilderScreen> createState() => _ScheduleBuilderScreenState();
}

class _ScheduleBuilderScreenState extends ConsumerState<ScheduleBuilderScreen> {
  bool _isToCampus = true;
  
  // Form State
  LatLng? _mapLocation;
  String? _campusChoice;
  String? _dayOfWeek;
  TimeOfDay? _time;
  String? _role;
  
  // Campuses fetched directly from repository during build

  
  final List<String> _days = [
    'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'
  ];

  final List<String> _roles = ['Hitchhiker', 'Driver', 'Rider'];

  Future<void> _pickLocation() async {
    final title = _isToCampus ? 'Departure Location' : 'Arrival Location';
    final result = await Navigator.of(context).push<LatLng>(
      MaterialPageRoute(builder: (_) => MapPickerScreen(title: title)),
    );
    if (result != null) {
      setState(() {
        _mapLocation = result;
      });
    }
  }

  Future<void> _pickTime() async {
    final result = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
      helpText: _isToCampus ? 'First Class Start Time' : 'Last Class End Time',
    );
    if (result != null) {
      setState(() {
        _time = result;
      });
    }
  }

  Future<void> _saveSchedule() async {
    if (_mapLocation == null || _campusChoice == null || _dayOfWeek == null || _time == null || _role == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please fill all fields')),
      );
      return;
    }
    
    final formattedTime = '${_time!.hour.toString().padLeft(2, '0')}:${_time!.minute.toString().padLeft(2, '0')}';
    
    try {
      await ref.read(tripActionProvider.notifier).saveTrip(
        isToCampus: _isToCampus,
        mapLocation: _mapLocation!,
        campus: _campusChoice!,
        dayOfWeek: _dayOfWeek!,
        scheduleTime: formattedTime,
        role: _role!,
      );
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Schedule Saved successfully!')),
        );
        Navigator.of(context).pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString())),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final tripActionState = ref.watch(tripActionProvider);
    final isLoading = tripActionState.isLoading;
    return Scaffold(
      appBar: AppBar(title: const Text('Build Weekly Schedule')),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          Row(
            children: [
              Expanded(
                child: RadioListTile<bool>(
                  title: const Text('To Campus'),
                  value: true,
                  groupValue: _isToCampus,
                  onChanged: (val) => setState(() {
                    _isToCampus = val!;
                    _mapLocation = null;
                  }),
                ),
              ),
              Expanded(
                child: RadioListTile<bool>(
                  title: const Text('From Campus'),
                  value: false,
                  groupValue: _isToCampus,
                  onChanged: (val) => setState(() {
                    _isToCampus = val!;
                    _mapLocation = null;
                  }),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          ListTile(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
              side: BorderSide(color: Theme.of(context).colorScheme.primary),
            ),
            title: Text(_isToCampus ? 'Departure Location' : 'Arrival Location'),
            subtitle: Text(_mapLocation != null 
                ? '${_mapLocation!.latitude.toStringAsFixed(4)}, ${_mapLocation!.longitude.toStringAsFixed(4)}' 
                : 'Tap to select on map'),
            trailing: const Icon(Icons.map),
            onTap: _pickLocation,
          ),
          const SizedBox(height: 16),
          DropdownButtonFormField<String>(
            decoration: const InputDecoration(labelText: 'Choose Campus'),
            value: _campusChoice,
            items: CampusRepository.allCampuses.map((c) => DropdownMenuItem(value: c.id, child: Text(c.displayName))).toList(),
            onChanged: (val) => setState(() => _campusChoice = val),
          ),
          const SizedBox(height: 16),
          const SizedBox(height: 16),
          DropdownButtonFormField<String>(
            decoration: const InputDecoration(labelText: 'Day of Week'),
            value: _dayOfWeek,
            items: _days.map((d) => DropdownMenuItem(value: d, child: Text(d))).toList(),
            onChanged: (val) => setState(() => _dayOfWeek = val),
          ),
          const SizedBox(height: 16),
          ListTile(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
              side: BorderSide(color: Theme.of(context).colorScheme.primary),
            ),
            title: Text(_isToCampus ? 'First Class Start Time' : 'Last Class End Time'),
            subtitle: Text(_time != null ? _time!.format(context) : 'Tap to select time'),
            trailing: const Icon(Icons.access_time),
            onTap: _pickTime,
          ),
          const SizedBox(height: 16),
          DropdownButtonFormField<String>(
            decoration: const InputDecoration(labelText: 'Role'),
            value: _role,
            items: _roles.map((r) => DropdownMenuItem(value: r, child: Text(r.toLocalizedRole(ref.watch(localeProvider))))).toList(),
            onChanged: (val) => setState(() => _role = val),
          ),
          const SizedBox(height: 32),
          ElevatedButton(
            onPressed: isLoading ? null : _saveSchedule,
            child: isLoading 
              ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
              : const Text('Save Schedule Item'),
          ),
        ],
      ),
    );
  }
}
