/**
 * 
 */
package elements;

import java.awt.Graphics2D;

import javax.vecmath.Vector2d;

import environment.Waypoint;

/**
 * @author camille
 *
 */
public class Vehicle extends Element {
	
	protected double damages;
	protected Vector2d speed;
	protected Waypoint currentWayPoint;
	
	
	@Override
	public void draw(Graphics2D g2d){
		// TODO
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
