/**
 * 
 */
package surface;

import java.awt.Polygon;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * @author camille
 * 
 */
public class Graph {

	public static final int PTS_RADIUS = 5;
	private static final int WALL_DISTANCE = 2;
	private static final int POINTS_PER_SEGMENT = 6;

	private Hashtable<Point2d, LinkedList<Point2d>> points;
	private Surface surface;
	private int nbPoints;
	private Random rand;

	public Graph(Surface surface, int nbPoints) {

		this.surface = surface;
		this.nbPoints = nbPoints;
		this.rand = new Random();

		points = new Hashtable<Point2d, LinkedList<Point2d>>();
		mapSurface();
	}

	public void addPoint(Point2d point) {
		points.put(point, new LinkedList<Point2d>());
	}

	public void addNeighbor(Point2d p, Point2d n) {
		LinkedList<Point2d> neighbors = points.get(p);
		neighbors.add(n);

		points.put(p, neighbors);
	}

	public Point2d getNearestPoint(Point2d p) {
		double dist = Integer.MAX_VALUE;
		Point2d nearest = null;

		for (Point2d contender : points.keySet()) {
			Vector2d distance = new Vector2d(p.x - contender.x, p.y - contender.y);

			if (distance.length() < dist) {
				dist = distance.length();
				nearest = contender;
			}
		}

		return nearest;
	}

	private void mapSurface() {

		for (Polygon p : surface.getObjects()) {
			for (int i = 0; i < p.npoints; i++) {
				Vector2d dir = new Vector2d(p.xpoints[(i + 1) % p.npoints] - p.xpoints[i], p.ypoints[(i + 1) % p.npoints] - p.ypoints[i]);
				double interval = dir.length() / POINTS_PER_SEGMENT;
				dir.normalize();

				if (interval != 0) {
					for (int j = 1; j < POINTS_PER_SEGMENT; j++) {
						Vector2d dist = new Vector2d(dir.x, dir.y);
						dist.scale(j * interval);

						Vector2d norm = new Vector2d((dir.x * Math.cos(Math.PI / 2.0d)) - (dir.y * Math.sin(Math.PI / 2.0d)), (dir.y * Math.cos(Math.PI / 2.0d)) + (dir.x * Math.sin(Math.PI / 2.0d)));
						norm.normalize();
						norm.scale(WALL_DISTANCE);

						addPoint(new Point2d(p.xpoints[i] + dist.x + norm.x, p.ypoints[i] + dist.y + norm.y));
					}
				}
			}
		}

		int pointsAdded = 0;

		while (pointsAdded < nbPoints) {

			int x = rand.nextInt(surface.wxsize);
			int y = rand.nextInt(surface.wxsize);

			Point2d p = new Point2d(x, y);

			if (surface.goodPoint(p)) {
				pointsAdded++;
				addPoint(p);
			}
		}

		connectGraph();
	}

	private void connectGraph() {
		for (Point2d p : points.keySet()) {
			for (Point2d contender : points.keySet()) {
				if (p != contender) {
					if (surface.canSee(p, contender)) {
						addNeighbor(p, contender);
					}
				}
			}
		}
	}

	public Hashtable<Point2d, LinkedList<Point2d>> getPoints() {
		return points;
	}
}
