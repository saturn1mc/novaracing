package battlefield.aStar;

import java.util.Hashtable;
import java.util.LinkedList;

import javax.vecmath.Point2d;

import battlefield.surface.Waypoint;


public class Path {
	private boolean solved;
	private LinkedList<Waypoint> path;
	private double distance;

	public Path(boolean s) {
		distance = Double.MAX_VALUE;
		solved = s;
		path = new LinkedList<Waypoint>();
	}

	public void constructPath(Hashtable<Point2d, Point2d> pere, Point2d depart, Point2d arrive) {
		Point2d p = arrive;

		while (p != null && p != depart) {
			Waypoint wp = new Waypoint(p);
			
			if(!path.isEmpty()){
				wp.setNext(path.getFirst());
				path.getFirst().setPrevious(wp);
				
				distance += AStar.distance(wp.getPosition(), path.getFirst().getPosition());
			}
			
			path.addFirst(wp);
			
			p = pere.get(p);
		}

		if (p != null) {
			Waypoint wp = new Waypoint(p);
			
			if(!path.isEmpty()){
				wp.setNext(path.getFirst());
				path.getFirst().setPrevious(wp);
				
				distance += AStar.distance(wp.getPosition(), path.getFirst().getPosition());
			}
			
			path.addFirst(wp);
		}
	}

	public void printPath() {
		for (Waypoint p : path) {
			System.out.println(p);
		}
	}

	public LinkedList<Waypoint> getPoints() {
		return path;
	}

	public boolean isSolved() {
		return solved;
	}
	
	public double getDistance(){
		return distance;
	}
}