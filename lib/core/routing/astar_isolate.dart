import 'dart:isolate';
import 'package:collection/collection.dart';
import 'package:latlong2/latlong.dart';
import 'graph_models.dart';
import 'graph_parser.dart';

class AStarIsolateData {
  final SendPort sendPort;
  final String geojsonString;
  final LatLng startLatLng;
  final LatLng endLatLng;
  // Module 6: Optional waypoints for multi-stop routing
  final List<LatLng> waypoints;

  AStarIsolateData(this.sendPort, this.geojsonString, this.startLatLng, this.endLatLng, {this.waypoints = const []});
}

class AStarResult {
  final List<LatLng> run1;
  final List<LatLng> run2;
  final List<LatLng> run3;
  final double totalDistanceInMeters;

  AStarResult({
    required this.run1, 
    required this.run2, 
    required this.run3,
    required this.totalDistanceInMeters,
  });
}

class AStarNode {
  final GraphNode node;
  final AStarNode? parent;
  final double gCost; // Cost from start
  final double hCost; // Heuristic cost to end
  final GraphEdge? edgeFromParent;

  AStarNode({
    required this.node,
    this.parent,
    required this.gCost,
    required this.hCost,
    this.edgeFromParent,
  });

  double get fCost => gCost + hCost;
}

void aStarIsolateMain(AStarIsolateData data) {
  final graph = GraphParser.parseGeoJson(data.geojsonString);
  final distanceCalc = const Distance();

  // Module 6: Multi-waypoint TSP routing
  if (data.waypoints.isNotEmpty) {
    _handleMultiWaypointRoute(data, graph, distanceCalc);
    return;
  }

  // Original single-path routing
  GraphNode? startNode = _findClosestNode(graph, data.startLatLng, distanceCalc);
  GraphNode? endNode = _findClosestNode(graph, data.endLatLng, distanceCalc);

  if (startNode == null || endNode == null) {
    data.sendPort.send(null);
    return;
  }

  // Run 1: Shortest Path
  final path1 = _runAStar(graph, startNode, endNode, distanceCalc);
  
  if (path1.isEmpty) {
    data.sendPort.send(AStarResult(
      run1: [startNode.location, endNode.location], 
      run2: [], 
      run3: [], 
      totalDistanceInMeters: distanceCalc.as(LengthUnit.Meter, startNode.location, endNode.location)
    ));
    return;
  }

  // Extract LatLngs
  List<LatLng> extractLatLngs(List<GraphEdge> edges) {
    if (edges.isEmpty) return [];
    final List<LatLng> pts = [edges.first.start.location];
    for (var edge in edges) {
      pts.add(edge.end.location);
    }
    return pts;
  }

  // Calculate total distance for run1
  double totalDistance = 0.0;
  for (var edge in path1) {
    totalDistance += distanceCalc.as(LengthUnit.Meter, edge.start.location, edge.end.location);
  }

  final result = AStarResult(
    run1: extractLatLngs(path1),
    run2: [], 
    run3: [], 
    totalDistanceInMeters: totalDistance,
  );

  data.sendPort.send(result);
}

/// Module 6: Handle multi-waypoint routing with TSP optimization
void _handleMultiWaypointRoute(AStarIsolateData data, Graph graph, Distance distanceCalc) {
  final startNode = _findClosestNode(graph, data.startLatLng, distanceCalc);
  final endNode = _findClosestNode(graph, data.endLatLng, distanceCalc);
  
  if (startNode == null || endNode == null) {
    data.sendPort.send(null);
    return;
  }

  // Find closest nodes for each waypoint
  final List<GraphNode> waypointNodes = [];
  for (final wp in data.waypoints) {
    final node = _findClosestNode(graph, wp, distanceCalc);
    if (node != null) {
      waypointNodes.add(node);
    }
  }

  if (waypointNodes.isEmpty) {
    // No valid waypoints, fall back to direct route
    final path = _runAStar(graph, startNode, endNode, distanceCalc);
    if (path.isEmpty) {
      data.sendPort.send(AStarResult(
        run1: [startNode.location, endNode.location],
        run2: [], run3: [],
        totalDistanceInMeters: distanceCalc.as(LengthUnit.Meter, startNode.location, endNode.location),
      ));
    } else {
      double dist = 0;
      for (var e in path) {
        dist += distanceCalc.as(LengthUnit.Meter, e.start.location, e.end.location);
      }
      data.sendPort.send(AStarResult(
        run1: _extractLatLngs(path), run2: [], run3: [],
        totalDistanceInMeters: dist,
      ));
    }
    return;
  }

  // Pre-compute A* paths between all relevant node pairs
  // Nodes: [start, ...waypoints, end]
  final allNodes = [startNode, ...waypointNodes, endNode];
  final n = allNodes.length;
  
  // Cache: pathCache[i][j] = { path: List<LatLng>, distance: double }
  final pathCache = List.generate(n, (_) => List<_CachedPath?>.filled(n, null));
  
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
      if (i == j) continue;
      final path = _runAStar(graph, allNodes[i], allNodes[j], distanceCalc);
      double dist = 0;
      for (var e in path) {
        dist += distanceCalc.as(LengthUnit.Meter, e.start.location, e.end.location);
      }
      pathCache[i][j] = _CachedPath(
        points: path.isEmpty ? [allNodes[i].location, allNodes[j].location] : _extractLatLngs(path),
        distance: path.isEmpty 
          ? distanceCalc.as(LengthUnit.Meter, allNodes[i].location, allNodes[j].location) * 2 // penalty for no-path
          : dist,
      );
    }
  }

  // TSP: Find best permutation of waypoint indices [1..n-2] (start=0, end=n-1)
  final waypointIndices = List.generate(waypointNodes.length, (i) => i + 1); // indices 1 to n-2
  
  List<int> bestPermutation = waypointIndices;
  double bestDistance = double.infinity;

  if (waypointIndices.length <= 6) {
    // Brute-force all permutations
    for (final perm in _permutations(waypointIndices)) {
      double totalDist = 0;
      final sequence = [0, ...perm, n - 1];
      for (int i = 0; i < sequence.length - 1; i++) {
        totalDist += pathCache[sequence[i]][sequence[i + 1]]!.distance;
      }
      if (totalDist < bestDistance) {
        bestDistance = totalDist;
        bestPermutation = List.from(perm);
      }
    }
  } else {
    // Greedy nearest-neighbor for > 6 waypoints
    final remaining = Set<int>.from(waypointIndices);
    final order = <int>[];
    int current = 0; // start node index
    
    while (remaining.isNotEmpty) {
      int nearest = remaining.first;
      double nearestDist = double.infinity;
      for (final idx in remaining) {
        final d = pathCache[current][idx]!.distance;
        if (d < nearestDist) {
          nearestDist = d;
          nearest = idx;
        }
      }
      order.add(nearest);
      remaining.remove(nearest);
      current = nearest;
    }
    
    bestPermutation = order;
    bestDistance = 0;
    final sequence = [0, ...order, n - 1];
    for (int i = 0; i < sequence.length - 1; i++) {
      bestDistance += pathCache[sequence[i]][sequence[i + 1]]!.distance;
    }
  }

  // Concatenate the best route path segments
  final bestSequence = [0, ...bestPermutation, n - 1];
  final List<LatLng> fullPath = [];
  
  for (int i = 0; i < bestSequence.length - 1; i++) {
    final segmentPoints = pathCache[bestSequence[i]][bestSequence[i + 1]]!.points;
    if (fullPath.isNotEmpty && segmentPoints.isNotEmpty) {
      // Avoid duplicate point at segment joins
      fullPath.addAll(segmentPoints.skip(1));
    } else {
      fullPath.addAll(segmentPoints);
    }
  }

  data.sendPort.send(AStarResult(
    run1: fullPath,
    run2: [],
    run3: [],
    totalDistanceInMeters: bestDistance,
  ));
}

/// Helper class for caching path results
class _CachedPath {
  final List<LatLng> points;
  final double distance;
  _CachedPath({required this.points, required this.distance});
}

/// Generate all permutations of a list
List<List<int>> _permutations(List<int> items) {
  if (items.isEmpty) return [[]];
  if (items.length == 1) return [List.from(items)];
  
  final List<List<int>> result = [];
  for (int i = 0; i < items.length; i++) {
    final current = items[i];
    final remaining = [...items.sublist(0, i), ...items.sublist(i + 1)];
    for (final perm in _permutations(remaining)) {
      result.add([current, ...perm]);
    }
  }
  return result;
}

/// Extract LatLngs from path edges
List<LatLng> _extractLatLngs(List<GraphEdge> edges) {
  if (edges.isEmpty) return [];
  final List<LatLng> pts = [edges.first.start.location];
  for (var edge in edges) {
    pts.add(edge.end.location);
  }
  return pts;
}

GraphNode? _findClosestNode(Graph graph, LatLng target, Distance distCalc) {
  GraphNode? closest;
  double minDst = double.infinity;
  for (var node in graph.nodes.values) {
    double d = distCalc.as(LengthUnit.Meter, node.location, target);
    if (d < minDst) {
      minDst = d;
      closest = node;
    }
  }
  return closest;
}

List<GraphEdge> _runAStar(Graph graph, GraphNode start, GraphNode end, Distance distCalc) {
  final openSet = PriorityQueue<AStarNode>((a, b) => a.fCost.compareTo(b.fCost));
  final openSetMap = <String, AStarNode>{};
  final closedSet = <String>{};

  final startAStarNode = AStarNode(
    node: start,
    gCost: 0,
    hCost: distCalc.as(LengthUnit.Meter, start.location, end.location),
  );

  openSet.add(startAStarNode);
  openSetMap[start.id] = startAStarNode;

  while (openSet.isNotEmpty) {
    final current = openSet.removeFirst();
    openSetMap.remove(current.node.id);

    if (current.node.id == end.id) {
      // Reconstruct path
      final List<GraphEdge> path = [];
      var curr = current;
      while (curr.parent != null) {
        if (curr.edgeFromParent != null) {
          path.insert(0, curr.edgeFromParent!);
        }
        curr = curr.parent!;
      }
      return path;
    }

    closedSet.add(current.node.id);

    final neighbors = graph.edges[current.node.id] ?? [];
    for (var edge in neighbors) {
      if (closedSet.contains(edge.end.id)) continue;

      final tentativeGCost = current.gCost + edge.cost; // edge.cost includes multiplier

      var neighborAStarNode = openSetMap[edge.end.id];

      if (neighborAStarNode == null || tentativeGCost < neighborAStarNode.gCost) {
        final newHCost = distCalc.as(LengthUnit.Meter, edge.end.location, end.location);
        final newNode = AStarNode(
          node: edge.end,
          parent: current,
          gCost: tentativeGCost,
          hCost: newHCost,
          edgeFromParent: edge,
        );

        if (neighborAStarNode != null) {
          // PriorityQueue doesn't support easy updates, so we add a new one.
          // This creates duplicates in PriorityQueue but openSetMap ensures we track the best.
          // It's a standard workaround for dart's PriorityQueue.
          openSet.add(newNode);
        } else {
          openSet.add(newNode);
        }
        openSetMap[edge.end.id] = newNode;
      }
    }
  }

  return []; // No path found
}
