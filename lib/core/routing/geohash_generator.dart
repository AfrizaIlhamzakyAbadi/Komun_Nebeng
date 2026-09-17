import 'package:dart_geohash/dart_geohash.dart';
import 'package:latlong2/latlong.dart';

class GeohashGenerator {
  static const double sampleDistanceMeters = 300.0;
  static const int geohashPrecision = 6;
  static final GeoHasher geoHasher = GeoHasher();

  /// Takes 3 routes, samples them every 300 meters, and returns a deduplicated
  /// list of 6-character Geohashes including their 8 neighbors.
  static List<String> generate1kmNet(List<List<LatLng>> routes) {
    final Set<String> geohashNet = {};

    for (var route in routes) {
      if (route.isEmpty) continue;

      final sampledPoints = _sampleRoute(route, sampleDistanceMeters);
      
      for (var point in sampledPoints) {
        final hash = geoHasher.encode(point.longitude, point.latitude, precision: geohashPrecision);
        geohashNet.add(hash);
        
        final neighbors = geoHasher.neighbors(hash);
        geohashNet.addAll(neighbors.values);
      }
    }

    return geohashNet.toList();
  }

  /// Takes a single route, samples it every 15 meters, and returns ONLY the exact 9-char geohashes.
  /// (Does not calculate or include neighbors).
  static List<String> generatePrecisionGeohashes(List<LatLng> route) {
    final Set<String> geohashRoute = {};
    if (route.isEmpty) return [];

    final sampledPoints = _sampleRoute(route, 15.0); // 15 meters for high precision
    
    for (var point in sampledPoints) {
      final hash = geoHasher.encode(point.longitude, point.latitude, precision: 9);
      geohashRoute.add(hash);
    }

    return geohashRoute.toList();
  }

  static List<LatLng> _sampleRoute(List<LatLng> route, double intervalMeters) {
    if (route.isEmpty) return [];
    
    final List<LatLng> sampled = [route.first];
    final distanceCalc = const Distance();
    
    double distanceAccumulator = 0.0;
    
    for (int i = 0; i < route.length - 1; i++) {
      final p1 = route[i];
      final p2 = route[i + 1];
      
      final segmentDistance = distanceCalc.as(LengthUnit.Meter, p1, p2);
      
      if (distanceAccumulator + segmentDistance >= intervalMeters) {
        // Interpolate point
        double remainingToSample = intervalMeters - distanceAccumulator;
        double fraction = remainingToSample / segmentDistance;
        
        LatLng interpolated = _interpolate(p1, p2, fraction);
        sampled.add(interpolated);
        
        // Handle multiple samples on a long segment
        while (remainingToSample + intervalMeters <= segmentDistance) {
          remainingToSample += intervalMeters;
          fraction = remainingToSample / segmentDistance;
          sampled.add(_interpolate(p1, p2, fraction));
        }
        
        distanceAccumulator = segmentDistance - remainingToSample;
      } else {
        distanceAccumulator += segmentDistance;
      }
    }
    
    // Add the last point just in case to cover the end
    if (sampled.last != route.last) {
      sampled.add(route.last);
    }
    
    return sampled;
  }

  static LatLng _interpolate(LatLng p1, LatLng p2, double fraction) {
    final lat = p1.latitude + (p2.latitude - p1.latitude) * fraction;
    final lng = p1.longitude + (p2.longitude - p1.longitude) * fraction;
    return LatLng(lat, lng);
  }
}
