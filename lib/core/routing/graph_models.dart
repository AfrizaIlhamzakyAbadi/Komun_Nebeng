import 'package:latlong2/latlong.dart';

class GraphNode {
  final String id;
  final LatLng location;

  GraphNode({required this.id, required this.location});

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is GraphNode && runtimeType == other.runtimeType && id == other.id;

  @override
  int get hashCode => id.hashCode;
}

class GraphEdge {
  final GraphNode start;
  final GraphNode end;
  final double distance;
  double costMultiplier; // Used for the 3-Route Net penalty

  GraphEdge({
    required this.start,
    required this.end,
    required this.distance,
    this.costMultiplier = 1.0,
  });

  double get cost => distance * costMultiplier;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is GraphEdge &&
          runtimeType == other.runtimeType &&
          start == other.start &&
          end == other.end;

  @override
  int get hashCode => start.hashCode ^ end.hashCode;
}

class Graph {
  final Map<String, GraphNode> nodes = {};
  // Adjacency list: node.id -> list of edges starting from this node
  final Map<String, List<GraphEdge>> edges = {};

  void addNode(GraphNode node) {
    if (!nodes.containsKey(node.id)) {
      nodes[node.id] = node;
      edges[node.id] = [];
    }
  }

  void addEdge(GraphNode start, GraphNode end, double distance, {bool isOneWay = false}) {
    final edge1 = GraphEdge(start: start, end: end, distance: distance);
    edges[start.id]?.add(edge1);

    if (!isOneWay) {
      final edge2 = GraphEdge(start: end, end: start, distance: distance);
      edges[end.id]?.add(edge2);
    }
  }
}
