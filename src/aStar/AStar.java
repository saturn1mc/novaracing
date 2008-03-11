package aStar;

import java.util.Hashtable;
import java.util.LinkedList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import surface.Graph;

public class AStar {

	private Graph graph;
	private Stack waitList;
	private Hashtable<Point2d, Point2d> parent;

	public AStar(Graph g, int stackSize) {
		graph = g;
		waitList = new Stack(stackSize);
		parent = new Hashtable<Point2d, Point2d>();
	}

	public double distance(Point2d a, Point2d b) {
		Vector2d v = new Vector2d(a.x - b.x, a.y - b.y);
		return v.length();
	}

	public Path solve(Point2d start, Point2d goal) {

		parent = new Hashtable<Point2d, Point2d>();
		waitList.clear();

		Node cursor;
		Point2d point;
		Point2d last = null;
		double currentDistance = 0;
		LinkedList<Point2d> parents;
		Path path;

		parents = graph.getPoints().get(start);

		for (Point2d p : parents) {
			parent.put(p, start);
			waitList.push(p, distance(start, p) + distance(p, goal));
		}

		last = start;
		cursor = waitList.pop();

		while (cursor != null && cursor.getPoint() != goal) {
			point = cursor.getPoint();
			currentDistance += distance(last, point);
			parents = graph.getPoints().get(point);
			for (Point2d p : parents) {
				if (parent.get(p) == null) {
					parent.put(p, point);
					waitList.push(p, currentDistance + distance(p, goal));
				}

			}
			last = point;
			cursor = waitList.pop();
		}

		if (cursor != null) {
			path = new Path(true);
			path.constructPath(parent, start, cursor.getPoint());
		} else {
			System.out.println("Failure : unconnected graph");
			path = new Path(false);
		}

		return path;
	}

}