package battlefield.surface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.LinkedList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.aStar.AStar;
import battlefield.aStar.Path;

public class Surface {

	private Rectangle area;
	private LinkedList<Polygon> objects; // The objects on the surface
	private Graph graph;
	private AStar aStar;

	public Surface(int width, int height, int nbPoints) {

		area = new Rectangle(width, height);

		objects = new LinkedList<Polygon>();

		//Obstacles

		Polygon p1 = new Polygon();
		p1.addPoint(100, 200);
		p1.addPoint(100, 250);
		p1.addPoint(200, 250);
		p1.addPoint(200, 200);
		objects.add(p1);

		Polygon p2 = new Polygon();
		p2.addPoint(300, 500);
		p2.addPoint(300, 550);
		p2.addPoint(320, 550);
		p2.addPoint(320, 500);
		objects.add(p2);

		Polygon p3 = new Polygon();
		p3.addPoint(400, 200);
		p3.addPoint(300, 250);
		p3.addPoint(400, 250);
		objects.add(p3);

		Polygon p4 = new Polygon();
		p4.addPoint(500, 200);
		p4.addPoint(550, 220);
		p4.addPoint(500, 250);
		p4.addPoint(600, 250);
		p4.addPoint(600, 200);
		objects.add(p4);

		Polygon p5 = new Polygon();
		p5.addPoint(500, 400);
		p5.addPoint(550, 420);
		p5.addPoint(500, 450);
		p5.addPoint(600, 450);
		p5.addPoint(600, 400);
		objects.add(p5);

		Polygon p6 = new Polygon();
		p6.addPoint(400, 420);
		p6.addPoint(300, 480);
		p6.addPoint(400, 480);
		objects.add(p6);

		this.graph = new Graph(this, nbPoints);
		this.aStar = new AStar(graph, graph.getPoints().keySet().size());
	}

	public void draw(Graphics g) {

		//Drawing objects
		g.setColor(Color.BLACK);
		for (Polygon p : objects) {
			g.fillPolygon(p);
		}

		//Drawing graph connections
		g.setColor(Color.LIGHT_GRAY);
		for (Point2d p : graph.getPoints().keySet()) {
			for (Point2d parent : graph.getPoints().get(p)) {
				g.drawLine((int) p.x, (int) p.y, (int) parent.x, (int) parent.y);
			}
		}

		//Drawing graph nodes
		g.setColor(Color.RED);
		for (Point2d p : graph.getPoints().keySet()) {
			g.fillOval((int) (p.x - (Graph.PTS_RADIUS / 2.0d)), (int) (p.y - (Graph.PTS_RADIUS / 2.0d)), Graph.PTS_RADIUS, Graph.PTS_RADIUS);
		}
	}

	public boolean canSee(Point2d a, Point2d b) {

		for (Polygon p : objects) {
			Vector2d dir = new Vector2d(b.x - a.x, b.y - a.y);
			double dist = dir.length();
			dir.normalize();

			for (int i = 1; i < dist + 1; i++) {
				Point2d target = new Point2d(a.x + (dir.x * i), a.y + (dir.y * i));

				if (p.contains(target.x, target.y)) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean goodPoint(Point2d pt) {
		for (Polygon p : objects) {
			if (p.contains(pt.x, pt.y)) {
				return false;
			}
		}

		return true;
	}

	public synchronized Path solve(Point2d start, Point2d goal) {
		Point2d nearestStart = graph.getNearestPoint(start);
		Point2d nearestGoal = graph.getNearestPoint(goal);

		Path path = aStar.solve(nearestStart, nearestGoal);

		return path;
	}

	public LinkedList<Polygon> getObjects() {
		return objects;
	}

	public Graph getGraph() {
		return graph;
	}

	public AStar getAStar() {
		return aStar;
	}

	public Rectangle getArea() {
		return area;
	}
}
