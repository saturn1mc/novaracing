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
	 * @param element
	 * @return <code>true</code> if the vehicle is in the obstacle's activation zone, or else <code>false</code>
	 */
	public boolean isInZone(Element element){
		Vector2d dist = new Vector2d(position.x - element.getPosition().x, position.y - element.getPosition().y);
		return (dist.length() <= radius);
	}
	
	@Override
	public void draw(Graphics2D g2d){
		g2d.setPaint(Color.gray);
		g2d.fillOval((int)(position.x - (radius/2.0d)), (int)(position.y - (radius/2.0d)), (int)radius, (int)radius);
	}
	
	@Override
	public boolean avoidedBy(Vehicle vehicle) {
		return true;
	}
	
	public void effectOn(Vehicle vehicle) {
		if(isInZone(vehicle)){
			//TODO Damages = f(velocity)
			System.out.println(vehicle.getName() + " crashed");
			vehicle.setDamages(100);
			vehicle.setSpeed(0);
		}
	}
	
	@Override
	public void effectOn(HumanVehicle vehicle) {
		if(isInZone(vehicle)){
			//TODO Damages = f(velocity)
			System.out.println(vehicle.getName() + " crashed");
			vehicle.setSpeed(0);
		}
	}
}
