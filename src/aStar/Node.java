package aStar;

import javax.vecmath.Point2d;

public class Node {
	private Point2d point;
	private double distance;

	public Node(Point2d p, double d) {
		point = p;
		distance = d;
	}

	public Point2d getPoint() {
		return point;
	}

	public double getDistance() {
		return distance;
	}

}