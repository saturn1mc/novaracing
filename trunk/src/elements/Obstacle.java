/**
 * 
 */
package elements;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * @author camille
 *
 */
public class Obstacle extends Element {
	
	protected double radius;
	
	public Obstacle(String name, Point2d position, double radius) {
		super(name, position);
		this.radius = radius;
	}

	public boolean isInZone(Vehicle vehicle){
		Vector2d dist = new Vector2d(position.x - vehicle.getPosition().x, position.y - vehicle.getPosition().y);
		return (dist.length() <= radius);
	}
	
	@Override
	public void draw(Graphics2D g2d){
		// TODO Auto-generated method stub
		super.draw(g2d);
	}
}
