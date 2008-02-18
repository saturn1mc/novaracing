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

	protected double radius;
	protected Waypoint next;
	protected Waypoint previous;

	public Waypoint(Point2d position, double radius) {
		super(DEFAULT_NAME + "" + id++, position);
		this.radius = radius;
	}

	public boolean isReachedBy(Vehicle vehicle) {
		Vector2d dist = new Vector2d(position.x - vehicle.getPosition().x, position.y - vehicle.getPosition().y);
		return (dist.length() <= (radius + Vehicle.radius));
	}

	public Point2d nearestPointOnRoad(Point2d p) {

		Point2d onNext = null;
		Point2d onPrev = null;

		Point2d p1;
		Point2d p2;

		if (previous != null) {

			if (position.x < previous.position.x) {
				p1 = position;
				p2 = previous.position;
			} else {
				p1 = previous.position;
				p2 = position;
			}

			if (p.x >= p1.x && p.x <= p2.x) {
				double y = (((p2.y - p1.y) / (p2.x - p1.x)) * (p.x - p1.x)) + p1.y;
				onPrev = new Point2d(p.x, y);
			}
		}

		if (next != null) {

			if (position.x < next.position.x) {
				p1 = position;
				p2 = next.position;
			} else {
				p1 = next.position;
				p2 = position;
			}

			double y = (((p2.y - p1.y) / (p2.x - p1.x)) * (p.x - p1.x)) + p1.y;
			
			if (p.x >= p1.x && p.x <= p2.x) {
				onNext = new Point2d(p.x, y);
			}
			else{
				onNext = new Point2d(p.x, y);
				
				Vector2d correction = new Vector2d(onNext.x - position.x, onNext.y - position.y);
				
				correction.scale(2.0d);
				onNext.sub(correction);
			}
		}

		if (onNext != null && onPrev != null) {
			return onPrev;
		} else {
			if (onPrev != null) {
				return onPrev;
			} else {
				if (onNext != null) {
					return onNext;
				} else {
					return position;
				}
			}
		}
	}

	@Override
	public void draw(Graphics2D g2d) {
		g2d.setPaint(Color.green);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
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
