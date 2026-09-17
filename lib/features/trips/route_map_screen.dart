import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../core/routing/polyline_encoder.dart';

class RouteMapScreen extends StatelessWidget {
  final String? encodedPolyline;
  final List<LatLng> routePath1; // Fallback
  final String title;
  // Module 6: Support multiple waypoints
  final List<LatLng> waypoints;

  const RouteMapScreen({
    super.key,
    this.encodedPolyline,
    this.routePath1 = const [],
    this.title = 'Route Map',
    this.waypoints = const [],
  });

  @override
  Widget build(BuildContext context) {
    List<LatLng> polylinePointsList = [];

    if (encodedPolyline != null && encodedPolyline!.isNotEmpty) {
      polylinePointsList = PolylineEncoder.decode(encodedPolyline!);
    } else if (routePath1.isNotEmpty) {
      polylinePointsList = routePath1;
    }

    if (polylinePointsList.isEmpty) {
      return Scaffold(
        appBar: AppBar(title: Text(title)),
        body: const Center(child: Text('No route path available')),
      );
    }

    final boundsPoints = List<LatLng>.from(polylinePointsList);
    for (final wp in waypoints) {
      boundsPoints.add(wp);
    }
    final bounds = LatLngBounds.fromPoints(boundsPoints);

    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: SizedBox.expand(
        child: FlutterMap(
          options: MapOptions(
            initialCameraFit: CameraFit.bounds(
              bounds: bounds,
              padding: const EdgeInsets.all(50.0),
            ),
          ),
          children: [
          TileLayer(
            urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
            userAgentPackageName: 'com.example.komun_nebeng',
          ),
          PolylineLayer(
            polylines: [
              Polyline(
                points: polylinePointsList,
                color: Colors.green.withValues(alpha: 0.8),
                strokeWidth: 5.0,
              ),
            ],
          ),
          MarkerLayer(
            markers: [
              Marker(
                point: polylinePointsList.first,
                child: const Icon(Icons.my_location, color: Colors.blue, size: 40),
              ),
              Marker(
                point: polylinePointsList.last,
                child: const Icon(Icons.flag, color: Colors.blue, size: 40),
              ),
              // Module 6: Multiple waypoint markers
              for (int i = 0; i < waypoints.length; i++)
                Marker(
                  point: waypoints[i],
                  child: Badge(
                    label: Text('${i + 1}', style: const TextStyle(fontSize: 10)),
                    child: const Icon(Icons.person_pin_circle, color: Colors.red, size: 40),
                  ),
                ),
            ],
          ),
        ],
      ),
      ),
    );
  }
}
