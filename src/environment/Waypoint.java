/**
 * 
 */
package environment;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import elements.Element;
import elements.Vehicle;

/**
 * @author camille
 *
 */
public class Waypoint extends Element{
	
	private static final String DEFAULT_NAME = "waypoint";
	
	protected double radius;
	protected Waypoint next;
	protected Waypoint previous;
	

	public Waypoint(Point2d position, double radius) {
		super(DEFAULT_NAME, position);
		this.radius = radius;
	}

	public boolean hasReached(Vehicle vehicle){
		Vector2d dist = new Vector2d(position.x - vehicle.getPosition().x, position.y - vehicle.getPosition().y);
		return (dist.length() <= radius);
	}
	
	@Override
	public void draw(Graphics2D g2d){
		g2d.setPaint(Color.green);
		g2d.drawOval((int)(position.x - (radius/2.0d)), (int)(position.y - (radius/2.0d)), (int)radius, (int)radius);
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
