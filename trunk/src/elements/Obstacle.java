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
public class Obstacle extends Element {
	
	/**
	 * Activation zone radius of the obstacle
	 */
	protected double radius;
	
	public Obstacle(String name, Point2d position, double radius) {
		super(name, position);
		this.radius = radius;
	}

	/**
	 * Indicator presence of a vehicle in the activation zone
	 * @param vehicle
	 * @return <code>true</code> if the vehicle is in the obstacle's activation zone, or else <code>false</code>
	 */
	public boolean isInZone(Vehicle vehicle){
		Vector2d dist = new Vector2d(position.x - vehicle.getPosition().x, position.y - vehicle.getPosition().y);
		return (dist.length() <= radius);
	}
	
	@Override
	public void draw(Graphics2D g2d){
		g2d.setPaint(Color.red);
		g2d.drawOval((int)(position.x - (radius/2.0d)), (int)(position.y - (radius/2.0d)), (int)radius, (int)radius);
	}
	
	public void affectBy(Vehicle v) {
		//TODO
		//Effect of a vehicle on the obstacle
		//for example : the obstacle could be damaged by the vehicle
	}
	
	public void effectOn(Vehicle v) {
		//Effect on a vehicle
	}
}
