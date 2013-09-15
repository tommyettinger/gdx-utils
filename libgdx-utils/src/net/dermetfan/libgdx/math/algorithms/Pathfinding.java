package net.dermetfan.libgdx.math.algorithms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public abstract class Pathfinding {

	public abstract static class AStar {

		public static class Node {

			public Vector2 position;
			public Node parent;
			public Node[] neighbours;
			public float weight, heuristic, total = 0;
			public boolean blocked;

			public Node(float x, float y, float weight, boolean blocked) {
				position = new Vector2(x, y);
				this.weight = weight;
				this.blocked = blocked;
			}
			
			@Override
			public String toString() {
				return position.toString() + ", weight: " + weight + ", heuristic: " + heuristic + ", total: " + total + ", blocked: " + blocked + ", parent: {" + (parent == null) /*parent.toString()*/ + "}, neighbours: " + neighbours;
			}

		}
		
		private static final Array<Node> closed = new Array<Node>();

		public static Node[] findPath(Node[] nodes, Node start, Node end, Node[] output) {
			closed.clear();

			// calculate heuristics
			for(Node node : nodes)
				node.heuristic = node.position.dst(end.position);

			// main loop
			start.parent = start;
			int pathSize = 1;
			float lowestTotal;
			Node node = start;
			while(node != end) {
				node.total = node.parent.total + node.weight + node.heuristic;
				for(Node neighbour : node.neighbours) {
					neighbour.parent = node;
					if(closed.contains(neighbour, true))
						continue;
					else if(neighbour.blocked) {
						closed.add(neighbour);
						continue;
					}
					neighbour.total = neighbour.parent.total + neighbour.weight + neighbour.heuristic;
				}
				lowestTotal = Float.POSITIVE_INFINITY;
				for(Node neighbour : node.neighbours) {
					if(neighbour.total < lowestTotal) {
						lowestTotal = neighbour.total;
						// TODO the problem is here, this counts all neighbors that have a lower total than the previous neighbor on to the pathSize but it should only be added the one that has the lowest total of of all neighbors 
						node = neighbour;
						pathSize++;
					} else
						closed.add(neighbour);
				}
			}
			
			// find out path
			output = new Node[pathSize];
			while(node != start) {
				if(pathSize < 1) {
					System.out.println("pathSize is " + (pathSize - 1));
					break;
				}
				output[--pathSize] = node.parent;
				node = node.parent;
			}

			for(int i = 0; i < output.length; i++)
				System.out.println(i + ": " + output[i]);
			
			return output;
		}
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
