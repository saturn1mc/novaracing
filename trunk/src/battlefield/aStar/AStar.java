package battlefield.aStar;

import java.util.Hashtable;
import java.util.LinkedList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.surface.Graph;


public class AStar {

	private Graph graph;
	private Stack waitList;
	private Hashtable<Point2d, Point2d> parent;
	private Hashtable<Point2d, Double> node_distance;

	public AStar(Graph g, int stackSize) {
		graph = g;
		waitList = new Stack(200);
		parent = new Hashtable<Point2d, Point2d>();
		node_distance = new Hashtable<Point2d, Double>();
	}

	public static double distance(Point2d a, Point2d b) {
		Vector2d v = new Vector2d(a.x - b.x, a.y - b.y);
		return v.length();
	}

	public Path solve(Point2d start, Point2d goal) {
		node_distance = new Hashtable<Point2d, Double>();
		parent = new Hashtable<Point2d, Point2d>();
		waitList.clear();

		Node cursor;
		Point2d point;
		double distance_path = 1000000;
		double currentDistance = 0;
		double turnDistance = 0;
		LinkedList<Point2d> parents;
		Path path;

		parents = graph.getPoints().get(start);

		for (Point2d p : parents) {
			parent.put(p, start);
			node_distance.put(p, distance(start, p));	
			waitList.push(p, distance(start, p) + distance(p, goal), start);
		}

		cursor = waitList.pop();
		path = new Path(false);
		
		while (cursor != null && node_distance.get(cursor.getPoint()) < distance_path) {
			if (cursor.getPoint() != goal) {
				point = cursor.getPoint();
				currentDistance = node_distance.get(cursor.getPoint());
				parents = graph.getPoints().get(point);
				for (Point2d p : parents) {
					
					turnDistance = distance(point, p);
					double new_distance = currentDistance + turnDistance;
					if (new_distance < distance_path){
						if (parent.get(p) == null) {
							parent.put(p, point);
							node_distance.put(p, new_distance);	
							waitList.push(p, new_distance + distance(p, goal), point);
						} else if (node_distance.get(p) > new_distance){
							parent.put(point, cursor.getParent());
							node_distance.put(p, new_distance);	
							waitList.push(p, new_distance + distance(p, goal), point);
						}
					}
	
				}
				cursor = waitList.pop();
			} else {
				if (path.isSolved()==false){
					path = new Path(true);
					path.constructPath(parent, start, cursor.getPoint());
					distance_path = path.getDistance();
					
				} else {
					double actual_dist = path.getDistance();
					Path path2 = new Path(true);
					path2.constructPath(parent, start, cursor.getPoint());
					if (actual_dist > path2.getDistance()) {
						path = path2;
						distance_path = path2.getDistance();
					}
				}
				cursor = waitList.pop();
			}
		}
		return path;
	}

}