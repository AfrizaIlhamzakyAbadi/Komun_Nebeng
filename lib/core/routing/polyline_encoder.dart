import 'package:latlong2/latlong.dart';

class PolylineEncoder {
  static String encode(List<LatLng> path) {
    if (path.isEmpty) return "";

    final StringBuffer result = StringBuffer();
    int lastLat = 0;
    int lastLng = 0;

    for (final LatLng point in path) {
      int lat = (point.latitude * 1e5).round();
      int lng = (point.longitude * 1e5).round();

      int dLat = lat - lastLat;
      int dLng = lng - lastLng;

      _encodeValue(dLat, result);
      _encodeValue(dLng, result);

      lastLat = lat;
      lastLng = lng;
    }

    return result.toString();
  }

  static void _encodeValue(int value, StringBuffer result) {
    int shifted = value << 1;
    if (value < 0) {
      shifted = ~shifted;
    }
    
    while (shifted >= 0x20) {
      result.writeCharCode((0x20 | (shifted & 0x1f)) + 63);
      shifted >>= 5;
    }
    result.writeCharCode(shifted + 63);
  }

  static List<LatLng> decode(String polyline) {
    if (polyline.isEmpty) return [];
    
    List<LatLng> points = [];
    int index = 0, len = polyline.length;
    int lat = 0, lng = 0;

    while (index < len) {
      int b, shift = 0, result = 0;
      do {
        b = polyline.codeUnitAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;
      do {
        b = polyline.codeUnitAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      points.add(LatLng(lat / 1e5, lng / 1e5));
    }
    return points;
  }
}
