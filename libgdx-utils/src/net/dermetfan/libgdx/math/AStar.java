package net.dermetfan.libgdx.math;

import com.badlogic.gdx.math.Vector2;

public abstract class AStar {

	public static class Node {

		public Vector2 position;
		public float weight, g_cost, h_heuristic, f_totalCost;
		public boolean blocked;
		public Node parent, neighbors[];

		public Node(float x, float y, float weight, boolean blocked) {
			position = new Vector2(x, y);
			this.weight = weight;
			this.blocked = blocked;
		}

	}

	//		public static Node[] findPath(Node start, Node end, Node[] nodes) {
	////			OPEN = priority queue containing START
	//			Array<Node> open = new Array<Node>();
	//			open.add(start);
	////			CLOSED = empty set
	//			Array<Node> closed = new Array<Node>();
	////			while lowest rank in OPEN is not the GOAL:
	//			Node current;
	//			while(open.contains(end, true)) {
	////			  current = remove lowest rank item from OPEN
	//			  current = open.pop();
	////			  add current to CLOSED
	//			  closed.add(current);
	////			  for neighbors of current:
	//			  for(Node neighbor : current.neighbors) {
	////			    cost = g(current) + movementcost(current, neighbor)
	////			    if neighbor in OPEN and cost less than g(neighbor):
	////			      remove neighbor from OPEN, because new path is better
	////			    if neighbor in CLOSED and cost less than g(neighbor): **
	////			      remove neighbor from CLOSED
	////			    if neighbor not in OPEN and neighbor not in CLOSED:
	////			      set g(neighbor) to cost
	////			      add neighbor to OPEN
	////			      set priority queue rank to g(neighbor) + h(neighbor)
	////			      set neighbor's parent to current
	//			  }
	//			}
	////			reconstruct reverse path from goal to start
	////			by following parent pointers
	//			return null;
	//		}
	//		
	//	}
	//	
	//}
	//
	//		public static Node[] findPath(Node start, Node end, Node[] nodes) {
	//			Array<Node> open = new Array<Node>(), closed = new Array<Node>();
	//
	//			Node current;
	//
	//			open.add(start);
	//			long startTime = TimeUtils.millis();
	//			while(!closed.contains(end, true) && TimeUtils.millis() - startTime < 1000) {
	//
	//				current = findLowestFNode(open);
	//				closed.add(current);
	//
	//				for(Node neighbor : current.neighbors) {
	//					if(neighbor.blocked || closed.contains(neighbor, true))
	//						continue;
	//					if(!open.contains(neighbor, true))
	//						open.add(neighbor);
	//					neighbor.parent = current;
	//					neighbor.f_totalCost = (neighbor.g_cost = neighbor.parent.f_totalCost + neighbor.weight) + (neighbor.h_heuristic = neighbor.position.dst(end.position));
	//					if(open.contains(neighbor, true))
	//						if(neighbor.g_cost < neighbor.parent.g_cost) {
	//							neighbor.parent = current;
	//							neighbor.f_totalCost = (neighbor.g_cost = neighbor.parent.f_totalCost + neighbor.weight) + neighbor.h_heuristic;
	//						}
	//				}
	//
	//			}
	//			
	//			Array<Node> path = new Array<Node>(Node.class);
	//
	//			current = end;
	//			while(current != start) {
	//				path.add(current);
	//				current = current.parent;
	//			}
	//			path.add(current);
	//
	//			return path.toArray();
	//		}
	//
	//		public static Node findLowestFNode(Array<Node> nodes) {
	//			Node lowestFNode = nodes.first();
	//			for(Node node : nodes)
	//				if(node.f_totalCost < lowestFNode.f_totalCost)
	//					lowestFNode = node;
	//			return lowestFNode;
	//		}
	//
	//	}
	//}
	//
	public static Node[] findPath(Node start, Node end, Node[] nodes) {
		// calculate heuristics
		for(Node node : nodes)
			node.h_heuristic = node.position.dst(end.position);

		// find path
		int pathSize = 1;
		Node current = start, previous = start;
		while(current != end && pathSize < nodes.length) {
			current.parent = previous;

			// calculate neighbor total costs
			for(Node neighbor : current.neighbors)
				neighbor.f_totalCost = current.f_totalCost + neighbor.weight + neighbor.h_heuristic + (neighbor.blocked ? 1000 : 0);

			// get neighbor with lowest total cost
			Node cheapestNeighbor = current.neighbors[0];
			for(Node neighbor : current.neighbors)
				if(neighbor.f_totalCost < cheapestNeighbor.f_totalCost)
					cheapestNeighbor = neighbor;

			previous = current;
			current = cheapestNeighbor;
			pathSize++;
		}

		end.parent = previous;

		// trace back path // until current.parent is previous
		Node[] path = new Node[pathSize];

		// current is end
		// until current is start
		int i = pathSize - 1;
		while(current != start) {

			path[i] = current;
			current = current.parent;

			i--;
		}

		return path;
	}

}

//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//		public static class Node {
//
//			public Vector2 position;
//			public Node parent;
//			public Node[] neighbors;
//			public float weight, heuristic, total = 0;
//			public boolean blocked;
//
//			public Node(float x, float y, float weight, boolean blocked) {
//				position = new Vector2(x, y);
//				this.weight = weight;
//				this.blocked = blocked;
//			}
//
//			@Override
//			public String toString() {
//				return position.toString() + ", weight: " + weight + ", heuristic: " + heuristic + ", total: " + total + ", blocked: " + blocked + ", parent: " + (parent != null ? parent.position : false) /*parent.toString()*/+ ", neighbors: " + (neighbors != null);
//			}
//
//		}
//
//		private static final Array<Node> closed = new Array<Node>();
//
//		public static Node[] findPath(Node[] nodes, Node start, Node end, Node[] output) {
//			closed.clear();
//
//			// calculate heuristics
//			for(Node node : nodes)
//				node.heuristic = node.position.dst(end.position);
//
//			// main loop
//			start.parent = start;
//			int pathSize = 1;
//			float lowestTotal;
//			Node node = start, lowestTotalNeighbor = null;
//			while(node != end) {
//				node.total = node.parent.total + node.weight + node.heuristic;
//				for(Node neighbor : node.neighbors) {
//					neighbor.parent = node;
//					if(closed.contains(neighbor, true))
//						continue;
//					else if(neighbor.blocked) {
//						closed.add(neighbor);
//						continue;
//					}
//					neighbor.total = neighbor.parent.total + neighbor.weight + neighbor.heuristic;
//				}
//				lowestTotal = Float.POSITIVE_INFINITY;
//				for(Node neighbor : node.neighbors) {
//					if(closed.contains(neighbor, true))
//						continue;
//					if(neighbor.total < lowestTotal) {
//						lowestTotal = neighbor.total;
//						lowestTotalNeighbor = neighbor;
//						// TODO the problem is here, this counts all neighbors that have a lower total than the previous neighbor on to the pathSize but it should only be added the one that has the lowest total of of all neighbors 
//					} else
//						closed.add(neighbor);
//				}
//				node = lowestTotalNeighbor;
//				pathSize++;
//			}
//
//			// find out path
//			output = new Node[pathSize];
//			while(node != start) {
//				if(pathSize < 1) {
//					System.out.println("pathSize is " + (pathSize - 1));
//					break;
//				}
//				output[--pathSize] = node.parent;
//				node = node.parent;
//			}
//
//			for(int i = 0; i < output.length; i++)
//				System.out.println(i + ": " + output[i]);
//
//			return output;
//		}
//	}
//}

//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//public static Node[][] generateGridMap(float[][] weight, float blockedValue, boolean wrapX, boolean wrapY, boolean diagonals) {
//Node[][] map = new Node[weight.length][];
//int x, y;
//
//int neighbours = diagonals ? 8 : 4;
//for(x = 0; x < weight.length; x++) {
//	map[x] = new Node[weight[x].length];
//	for(y = 0; y < weight[x].length; y++)
//		map[x][y] = new Node(weight[x][y], weight[x][y] == blockedValue, neighbours);
//}
//
//for(x = 0; x < map.length; x++)
//	for(y = 0; y < map[x].length; y++) {
//		map[x][y].neighbours[0] = map[x][(y - 1 + map[x].length) % map[x].length]; // above
//		map[x][y].neighbours[1] = map[(x - 1 + map.length) % map.length][y]; // left
//		map[x][y].neighbours[2] = map[(x + 1) % map.length][y]; // right
//		map[x][y].neighbours[3] = map[x][(y + 1) % map[x].length]; // below
//	}
//
//if(diagonals)
//	for(x = 0; x < map.length; x++)
//		for(y = 0; y < map[x].length; y++) {
//			map[x][y].neighbours[4] = map[(x - 1 + map.length) % map.length][(y - 1 + map[x].length) % map[x].length]; // left above
//			map[x][y].neighbours[5] = map[(x + 1) % map.length][(y - 1 + map[x].length) % map[x].length]; // right above
//			map[x][y].neighbours[6] = map[(x - 1 + map.length) % map.length][(y + 1) % map[x].length]; // left below
//			map[x][y].neighbours[7] = map[(x + 1) % map.length][(y + 1) % map[x].length]; // right below
//		}
//
//return map;
//}
//
//public static Vector2[] findPathOnGrid(Node[][] map, int startX, int startY, int endX, int endY, float moveCost, float diagonalMoveCost) {
//open.clear();
//closed.clear();
//
//int size = 1, x, y;
//for(x = 0; x < map.length; x++) {
//	size++;
//	for(y = 0; y < map[x].length; y++) {
//		size++;
//		map[x][y].heuristic = Math.abs(endX - x) + Math.abs(endY - y);
//	}
//}
//
//open.ensureCapacity(size);
//closed.ensureCapacity(size);
//
//x = startX;
//y = startY;
//do {
//	closed.add(map[x][y]);
//	for(int n = 0; n < map[x][y].neighbours.length; n++)
//		if(!closed.contains(map[x][y].neighbours[n], true))
//			if(!open.contains(map[x][y].neighbours[n], true)) {
//				map[x][y].neighbours[n].parent = map[x][y];
//			} else {
//				map[x][y].neighbours[n].total = map[x][y].total + n > 3 ? diagonalMoveCost : moveCost;
//			}
//
//} while(open.size > 0);
//
//return null;
//}
