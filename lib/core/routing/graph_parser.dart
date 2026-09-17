import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:latlong2/latlong.dart';
import 'graph_models.dart';

class GraphParser {
  static Future<Graph> loadGraph(String assetPath) async {
    final String geojsonString = await rootBundle.loadString(assetPath);
    return parseGeoJson(geojsonString);
  }

  static Graph parseGeoJson(String geojsonString) {
    final Map<String, dynamic> data = jsonDecode(geojsonString);
    final Graph graph = Graph();
    final Distance distanceCalculator = const Distance();

    if (data['type'] == 'FeatureCollection') {
      final features = data['features'] as List<dynamic>;

      for (var feature in features) {
        final geometry = feature['geometry'];
        if (geometry != null && geometry['type'] == 'LineString') {
          final coordinates = geometry['coordinates'] as List<dynamic>;
          
          final properties = feature['properties'] as Map<String, dynamic>? ?? {};
          final isOneWay = properties['oneway'] == 'yes' || properties['oneway'] == true;
          
          GraphNode? previousNode;

          for (var coord in coordinates) {
            final double lng = (coord[0] as num).toDouble();
            final double lat = (coord[1] as num).toDouble();
            
            // Using a simple precision string for Node ID (e.g., to 5 decimal places to snap close nodes)
            final String nodeId = '${lat.toStringAsFixed(5)},${lng.toStringAsFixed(5)}';
            final location = LatLng(lat, lng);
            
            final currentNode = GraphNode(id: nodeId, location: location);
            graph.addNode(currentNode);

            if (previousNode != null) {
              final double dist = distanceCalculator.as(
                  LengthUnit.Meter, previousNode.location, currentNode.location);
              graph.addEdge(previousNode, currentNode, dist, isOneWay: isOneWay);
            }

            previousNode = currentNode;
          }
        }
      }
    }
    return graph;
  }
}
