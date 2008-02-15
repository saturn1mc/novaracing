/**
 * 
 */
package elements;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import environment.Environment;
import environment.Waypoint;

/**
 * @author camille
 *
 */
public class Vehicle extends Element {
	
	protected static final double radius = 15.0; 
	
	protected double damages;
	protected Vector2d speed;
	protected Waypoint target;
	
	public Vehicle(String name, Point2d position, Waypoint currentWayPoint) {
		super(name, position);
		this.speed = new Vector2d(0 , 0);
		this.target = currentWayPoint;
	}

	@Override
	public void draw(Graphics2D g2d){
		g2d.setPaint(Color.BLUE);
		g2d.drawOval((int)(position.x - (radius/2.0d)), (int)(position.y - (radius/2.0d)), (int)radius, (int)radius);
		drawVector(speed, g2d, Color.magenta);
	}
	
	private void drawVector(Vector2d v, Graphics2D g2d, Color c){
		g2d.setPaint(c);
		g2d.drawLine((int)position.x, (int)position.y, (int)(position.x + v.x), (int)(position.y + v.y));
		g2d.drawOval((int)(position.x + v.x - 2.5), (int)(position.y + v.y - 2.5), 5, 5);
	}
	
	@Override
	public void update(Environment env) {
		this.position = target.getPosition();
		this.target = target.getNext();
		steeringForSeek();
	}
	
	private void steeringForSeek(){
		speed.set(target.getPosition().x - position.x, target.getPosition().y - position.y);
		speed.normalize();
		speed.scale(20.0);
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
		return target;
	}

	public void setCurrentWayPoint(Waypoint currentWayPoint) {
		this.target = currentWayPoint;
	}
}
