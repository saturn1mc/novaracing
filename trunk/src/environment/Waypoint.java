/**
 * 
 */
package environment;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import elements.Element;
import elements.Vehicle;

/**
 * @author camille
 *
 */
public abstract class Waypoint extends Element{
	
	protected double radius;
	protected Waypoint next;
	protected Waypoint previous;
	
	public Waypoint(Point2d position, double radius, Waypoint next, Waypoint previous) {
		super();
		this.radius = radius;
		this.next = next;
		this.previous = previous;
	}

	public boolean hasReached(Vehicle vehicle){
		Vector2d dist = new Vector2d(position.x - vehicle.getPosition().x, position.y - vehicle.getPosition().y);
		return (dist.length() <= radius);
	}
	
	@Override
	public void draw(Graphics2D g2d){
		g2d.drawOval((int)position.x, (int)position.y, (int)radius, (int)radius);
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
