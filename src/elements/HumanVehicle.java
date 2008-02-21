/**
 * 
 */
package elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import environment.Environment;

/**
 * @author camille
 *
 */
public class HumanVehicle extends Element {

	/**
	 * Vehicle's radius
	 */
	public static final double radius = 15.0d;

	/**
	 * Vehicle's mass
	 */
	private static final double mass = 1.0d;

	
	/**
	 * Vehicle's maximum speed
	 */
	private static final double maxSpeed = 1.0d;
	
	/**
	 * Vehicle's acceleration
	 */
	private final double accel = 0.1d;
	
	/**
	 * Vehicle's speed
	 */
	private double speed = 0.0d;

	/**
	 * Maximum force that can be applied to the vehicle
	 */
	private static final double maxForce = 0.05d;

	/**
	 * Steering force
	 */
	private Vector2d steering;
	
	/**
	 * Maximum correction angle
	 */
	private static final double maxCorrection = Math.PI / 2.0d;
	
	/**
	 * Steering coeff
	 */
	private static final double steer = 0.5d;
	
	/**
	 * Current correction angle
	 */
	private double correction = 0.0d;

	/**
	 * Velocity force
	 */
	private Vector2d velocity;
	
	/**
	 * Current target
	 */
	private Waypoint target;
	
	/**
	 * Keyboard listener
	 */
	private KeyAdapter keyboard;

	public HumanVehicle(String name, Point2d position, Waypoint target) {
		super(name, new Point2d(position));
		
		this.target = target;
		steering = new Vector2d(0, 0);
		correction = 0;
		velocity = new Vector2d(0, 0);
		
		keyboard = new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode() == KeyEvent.VK_LEFT){
					correction -= maxCorrection * steer;
					correction %= 2.0d * Math.PI;
				}
				
				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
					correction += maxCorrection * steer;
					correction %= 2.0d * Math.PI;
				}
				
				if(e.getKeyCode() == KeyEvent.VK_UP){
					if(speed < maxSpeed){
						speed += maxSpeed * accel;
					}
				}
				
				if(e.getKeyCode() == KeyEvent.VK_DOWN){
					if(speed >= 0.0d){
						speed -= maxSpeed * accel;
					}
				}
			}
		};
	}

	@Override
	public void update(Environment env) {

		//TODO take the other elements effects into account
		//(collision, bonuses, obstacles, ...)

		updateForces();
		
		for(Element e : env.getElements()){
			if(e != this){
				e.effectOn(this);
			}
		}
		
		/* Updating position */
		position.add(velocity);
	}

	private void updateForces() {

		/* Changing target */
		if (target.isReachedBy(this)) {
			this.target = target.getNext();
		}
		
		/* Correcting current steering */
		steering.x = Math.cos(correction);
		steering.y = Math.sin(correction);
		steering.normalize();
		
		/* Computing acceleration */
		Vector2d steeringForce = new Vector2d(truncate(steering, maxForce));
		Vector2d acceleration = new Vector2d(steeringForce);
		acceleration.scale(1.0d / mass);

		/* Computing velocity */
		Vector2d s = new Vector2d(velocity);
		s.add(acceleration);

		velocity.set(truncate(s, speed));
	}

	private Vector2d truncate(Vector2d v, double max) {

		Vector2d vT = new Vector2d(v);
		double l = vT.length();

		if (l > max) {
			vT.scale(max / l);
		}

		return vT;
	}
	
	public KeyAdapter getKeyboard(){
		return keyboard;
	}
	
	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}
	
	@Override
	public double getRadius() {
		return radius;
	}
	
	/* ------------------------- */
	/* ---      Avoidance    --- */
	/* ------------------------- */
	@Override
	public boolean avoidedBy(Vehicle vehicle) {
		return true;
	}

	@Override
	public boolean avoidedBy(HumanVehicle vehicle) {
		return true;
	}
	
	/* ------------------------- */
	/* ---     Influences    --- */
	/* ------------------------- */
	@Override
	public void effectOn(Vehicle vehicle) {
		
		Vector2d distance = new Vector2d(vehicle.position.x - position.x, vehicle.position.y - position.y);
		
		if(distance.length() < radius){
			vehicle.setVelocity(distance);
			velocity.negate();
		}
	}
	
	@Override
	public void effectOn(HumanVehicle vehicle) {
		Vector2d distance = new Vector2d(vehicle.position.x - position.x, vehicle.position.y - position.y);
		
		if(distance.length() < (vehicle.radius + radius)){
			vehicle.setVelocity(distance);
		}
	}
	
	/* ------------------------- */
	/* --- Drawing functions --- */
	/* ------------------------- */
	
	@Override
	public void draw(Graphics2D g2d) {
		
		/* The vehicle */
		g2d.setPaint(Color.red);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		g2d.fillOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		
		/* Its velocity */
		drawVector(g2d, velocity, Color.magenta, 20.0d);
		
		/* Its name */
		g2d.setPaint(Color.white);
		g2d.drawString(name, (float)(position.x + radius), (float)position.y);
	}
	
	/**
	 * Draws a scaled {@link Vector2d}
	 * @param g2d
	 * @param v
	 * @param c
	 * @param scale
	 */
	private void drawVector(Graphics2D g2d, Vector2d v, Color c, double scale) {
		g2d.setPaint(c);
		g2d.drawLine((int) position.x, (int) position.y, (int) (position.x + (v.x * scale)), (int) (position.y + (v.y * scale)));
		g2d.fillOval((int) (position.x + (v.x * scale) - 2.5), (int) (position.y + (v.y * scale) - 2.5), 5, 5);
	}
}
