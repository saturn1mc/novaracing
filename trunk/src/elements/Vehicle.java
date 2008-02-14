/**
 * 
 */
package elements;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import environment.Waypoint;

/**
 * @author camille
 *
 */
public class Vehicle extends Element {
	
	protected static final double radius = 15.0; 
	
	protected double damages;
	protected Vector2d speed;
	protected Waypoint currentWayPoint;
	
	public Vehicle(String name, Point2d position, Waypoint currentWayPoint) {
		super(name, position);
		this.currentWayPoint = currentWayPoint;
	}

	@Override
	public void draw(Graphics2D g2d){
		g2d.setPaint(Color.BLUE);
		g2d.drawOval((int)(position.x - (radius/2.0d)), (int)(position.y - (radius/2.0d)), (int)radius, (int)radius);
	}

	public double getDamages() {
		return damages;
	}

	public void setDamages(double damages) {
		this.damages = damages;
	}

	public Vector2d getSpeed() {
		return speed;
	}

	public void setSpeed(Vector2d speed) {
		this.speed = speed;
	}

	public Waypoint getCurrentWayPoint() {
		return currentWayPoint;
	}

	public void setCurrentWayPoint(Waypoint currentWayPoint) {
		this.currentWayPoint = currentWayPoint;
	}
}
