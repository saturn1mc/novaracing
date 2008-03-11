package aStar;

import javax.vecmath.Point2d;

public class Node {
	private Point2d point;
	private double distance;
	private Point2d parent;

	public Node(Point2d p, double d, Point2d papa) {
		point = p;
		distance = d;
		parent = papa;
	}

	public Point2d getPoint() {
		return point;
	}

	public double getDistance() {
		return distance;
	}

	public Point2d getParent() {
		return parent;
	}
	
	public void setParent(Point2d p) {
		parent = p;
	}
	
	public void setDistance(double d) {
		distance = d;
	}

}