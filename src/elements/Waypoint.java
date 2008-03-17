/**
 * 
 */
package elements;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import environment.Environment;

/**
 * @author camille
 * 
 */
public class Waypoint extends Element {

	private static int id = 0;
	private static final String DEFAULT_NAME = "waypoint_";

	/**
	 * Waypoints radius
	 */
	public static final double radius = 30.0d;

	/**
	 * Next waypoint (could be null)
	 */
	protected Waypoint next;

	/**
	 * Previous waypoint (could be null)
	 */
	protected Waypoint previous;

	public Waypoint(Point2d position) {
		super(DEFAULT_NAME + "" + id++, position);
	}

	/**
	 * Indicates if a vehicle as reached the {@link Waypoint}
	 * 
	 * @param vehicle
	 * @return <code>true</code> if the element as reached the
	 *         {@link Waypoint}, or else <code>false</code>
	 */
	public boolean isReachedBy(Element element) {
		Vector2d dist = new Vector2d(position.x - element.getPosition().x, position.y - element.getPosition().y);
		return (dist.length() <= radius);
	}

	/**
	 * Return the nearest point on the road corresponding to the given point.
	 * 
	 * @param p
	 * @return the nearest point on the road (i.e. on one of the segment : (<code>this.position</code>-<code>previous.position</code>)
	 *         or (<code>this.position</code>-<code>next.position</code>))
	 */
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
				// Projection is outside segment AB
				// TODO test on which side it is
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

	public Vector2d getNextDirection() {

		Vector2d direction = new Vector2d(0, 0);

		if (next != null) {
			direction.set(next.position.x - position.x, next.position.y - position.y);
			direction.normalize();
		}

		return direction;
	}

	public Vector2d getPreviousDirection() {

		Vector2d direction = new Vector2d(0, 0);

		if (previous != null) {
			direction.set(previous.position.x - position.x, previous.position.y - position.y);
			direction.normalize();
		}

		return direction;
	}

	public boolean onRoad(Element element) {
		if (previous != null) {

			Point2d nearest = null;
			Point2d A = position;
			Point2d B = previous.position;

			double L = Math.sqrt(((B.x - A.x) * (B.x - A.x)) + ((B.y - A.y) * (B.y - A.y)));
			double R = (((A.y - element.position.y) * (A.y - B.y)) - ((A.x - element.position.x) * (B.x - A.x))) / (L * L);

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
				return false;
			}

			Vector2d dist = new Vector2d(element.position.x - nearest.x, element.position.y - nearest.y);

			return (dist.length() <= (radius + Vehicle.radius));

		} else {
			return false;
		}
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
	
	@Override
	public String toString() {
		return "Waypoint(" + name + ") : " + position.x + " - " + position.y;
	}

	@Override
	public void update(Environment env) {
		// nothing...
	}
}
