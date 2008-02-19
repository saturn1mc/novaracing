/**
 * 
 */
package elements;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * @author camille
 * 
 */
public class Waypoint extends Element {

	private static int id = 0;
	private static final String DEFAULT_NAME = "waypoint_";

	public static final double radius = 30.0d;

	protected Waypoint next;
	protected Waypoint previous;

	public Waypoint(Point2d position) {
		super(DEFAULT_NAME + "" + id++, position);
	}

	public boolean isReachedBy(Vehicle vehicle) {
		Vector2d dist = new Vector2d(position.x - vehicle.getPosition().x, position.y - vehicle.getPosition().y);
		return (dist.length() <= radius);
	}

	public Point2d nearestPointOnRoad(Point2d p) {

		if (previous != null) {

			Point2d nearest = null;
			Point2d A = position;
			Point2d B = previous.position;

			double L = Math.sqrt(((B.x - A.x) * (B.x - A.x)) + ((B.y - A.y) * (B.y - A.y)));
			double R = (((A.y - p.y) * (A.y - B.y)) - ((A.x - p.x) * (B.x - A.x))) / (L * L);

			if (R == 0) {
				// Projection is on A
				nearest = new Point2d(A.x, A.y);
			} else if (R == 1) {
				// Projection is on B
				nearest = new Point2d(B.x, B.y);
			} else if (0 < R && R < 1) {
				// Projection is into segment AB
				nearest = new Point2d(A.x + (R * (B.x - A.x)), A.y + (R * (B.y - A.y)));
			} else {

				if (next != null) {
					double x = A.x + (R * (B.x - A.x));
					double y = A.y + (R * (B.y - A.y));

					double dist = new Vector2d(x - position.x, y - position.y).length();

					Vector2d nextDir = new Vector2d(next.position.x - position.x, next.position.y - position.y);
					nextDir.normalize();

					nearest = new Point2d(position.x + (nextDir.x * dist), position.y + (nextDir.y * dist));
				}
			}

			return nearest;

		} else {
			return null;
		}
	}

	@Override
	public void draw(Graphics2D g2d) {
		g2d.setPaint(Color.magenta);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
	}

	public Waypoint getNext() {
		return next;
	}

	public void setNext(Waypoint next) {
		this.next = next;
	}

	public Waypoint getPrevious() {
		return previous;
	}

	public void setPrevious(Waypoint previous) {
		this.previous = previous;
	}
}
