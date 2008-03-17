/**
 * 
 */
package nova.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import nova.environment.Environment;


/**
 * @author camille
 * 
 */
public class Vehicle extends Element {

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
	 * Vehicle's current speed
	 */
	private double speed = 0.0d;

	/**
	 * Maximum force that can be applied to the vehicle
	 */
	private static final double maxForce = 0.05d;

	/**
	 * Number of frame(s) to anticipate the movement
	 */
	private static final double predictionCoeff = 60.0d;

	/**
	 * Steering force
	 */
	private Vector2d steering;

	/**
	 * Current correction
	 */
	private Vector2d correction;

	/**
	 * Velocity force
	 */
	private Vector2d velocity;

	/**
	 * Local space X axis
	 */
	private Vector2d forward;

	/**
	 * Local space Y axis
	 */
	private Vector2d side;

	/**
	 * Predicted position
	 */
	private Point2d futurePosition;

	/**
	 * Nearest point on road corresponding to
	 */
	private Point2d nearestPointOnRoad;

	/**
	 * Vehicle's sight
	 */
	private Polygon sight;

	/**
	 * Current target
	 */
	private Waypoint target;

	/**
	 * Damages
	 */
	private double damages;

	/**
	 * Current bonus element
	 */
	//private Element bonus;
	public Vehicle(String name, Point2d position, Waypoint target) {
		super(name, position);

		this.forward = new Vector2d(0, 0);
		this.side = new Vector2d(0, 0);

		this.velocity = new Vector2d(0, 0);
		this.futurePosition = new Point2d(0, 0);
		this.target = target;
		this.sight = new Polygon();

		this.steering = new Vector2d(target.getPosition().x - position.x, target.getPosition().y - position.y);
		steering.normalize();

		this.correction = new Vector2d(0, 0);
	}

	@Override
	public void update(Environment env) {

		forward.set(velocity);
		forward.normalize();

		side.set((forward.x * Math.cos(Math.PI / 2.0d)) - (forward.y * Math.sin(Math.PI / 2.0d)), (forward.y * Math.cos(Math.PI / 2.0d)) + (forward.x * Math.sin(Math.PI / 2.0d)));
		side.normalize();

		//TODO take the other elements effects into account
		//(collision, bonuses, obstacles, ...)
		updateForces();

		updateFuturePosition();
		computeTrajectoryCorrection(env);

		for (Element e : env.getElements()) {
			if (e != this) {
				e.effectOn(this);
			}
		}

		/* Updating position */
		position.add(velocity);

		/* Updating sight */
		sight.reset();
		sight.addPoint((int) (position.x + (side.x * (radius / 2.0d))), (int) (position.y + (side.y * (radius / 2.0d))));
		sight.addPoint((int) (position.x - (side.x * (radius / 2.0d))), (int) (position.y - (side.y * (radius / 2.0d))));
		sight.addPoint((int) (futurePosition.x - (side.x * (radius / 2.0d))), (int) (futurePosition.y - (side.y * (radius / 2.0d))));
		sight.addPoint((int) (futurePosition.x + (side.x * (radius / 2.0d))), (int) (futurePosition.y + (side.y * (radius / 2.0d))));
	}

	/**
	 * Computes the forces to apply to the vehicle
	 * (steering direction, acceleration and velocity)
	 */
	private void updateForces() {

		if(target.onRoad(this)){
			if(speed < maxSpeed){
				speed += 0.01d * maxSpeed;
			}
		}
		else{
			if(speed > maxSpeed * 0.2d){
				speed -= 0.003d * maxSpeed;
			}
		}
		
		/* Changing target */
		if (target.isReachedBy(this)) {
			this.target = target.getNext();
		}

		/* Correcting current steering */
		steering.add(truncate(correction, maxForce));
		steering.normalize();

		/* Computing acceleration */
		Vector2d steeringForce = new Vector2d(truncate(steering, maxForce));
		Vector2d acceleration = new Vector2d(steeringForce);
		acceleration.scale(1.0d / mass);

		/* Computing velocity */
		Vector2d vForce = new Vector2d(velocity);
		vForce.add(acceleration);

		velocity.set(truncate(vForce, speed));
	}

	/**
	 * Updates the future position point
	 * (the position of the vehicle in <code>predictionCoeff</code> frames if it stays on the same direction)
	 */
	private void updateFuturePosition() {
		futurePosition.set(position.x + (velocity.x * predictionCoeff), position.y + (velocity.y * predictionCoeff));
	}

	/**
	 * Computes the correction to apply to the current steering
	 * @param env
	 */
	private void computeTrajectoryCorrection(Environment env) {

		boolean avoiding = false;

		correction.set(0, 0);
		nearestPointOnRoad = target.nearestPointOnRoad(futurePosition);

		/*
		 * Corrections to avoid obstacles and other vehicles
		 */
		for (Element e : env.getElements()) {
			if (e != this) {
				if (e.avoidedBy(this)) {

					Rectangle bbox = new Rectangle((int) (e.position.x - (e.getRadius() / 2.0d)), (int) (e.position.y - (e.getRadius() / 2.0d)), (int) radius, (int) radius);
					Rectangle intersection = bbox.intersection(sight.getBounds());

					/* If the element is on the way */
					if (!intersection.isEmpty()) {

						avoiding = true;

						Point2d A = position;
						Point2d B = futurePosition;

						double L = Math.sqrt(((B.x - A.x) * (B.x - A.x)) + ((B.y - A.y) * (B.y - A.y)));
						double S = (((A.y - e.position.y) * (B.x - A.x)) - ((A.x - e.position.x) * (B.y - A.y))) / (L * L);

						Vector2d avoidanceCorrection;

						if (S > 0) {
							avoidanceCorrection = new Vector2d(side.x, side.y);
						} else {
							avoidanceCorrection = new Vector2d(-side.x, -side.y);
						}

						avoidanceCorrection.scale(intersection.getWidth());
						correction.add(avoidanceCorrection);
					}
				}
			}
		}

		/*
		 * Correction to stay on road
		 */
		Vector2d roadCorrection = new Vector2d(nearestPointOnRoad.x - futurePosition.x, nearestPointOnRoad.y - futurePosition.y);

		/* If the future position is not on the road*/
		if (roadCorrection.length() > Waypoint.radius && !avoiding) {
			/* No correction is needed */
			correction.add(roadCorrection);
		}
	}

	private Vector2d truncate(Vector2d v, double max) {

		Vector2d vT = new Vector2d(v);
		double l = vT.length();

		if (l > max) {
			vT.scale(max / l);
		}

		return vT;
	}

	public double getDamages() {
		return damages;
	}

	public void setDamages(double damages) {
		this.damages = damages;
	}

	public Vector2d getSpeed() {
		return steering;
	}

	public void setSteering(Vector2d speed) {
		this.steering = speed;
	}
	
	public void setSpeed(double speed){
		this.speed = speed;
	}

	public Waypoint getCurrentTarget() {
		return target;
	}

	public void setCurrentWayPoint(Waypoint currentWayPoint) {
		this.target = currentWayPoint;
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

		if (distance.length() < radius) {
			System.out.println("Collision between " + name + " & " + vehicle.getName());

			vehicle.setVelocity(distance);
			velocity.negate();
		}
	}

	@Override
	public void effectOn(HumanVehicle vehicle) {
		Vector2d distance = new Vector2d(vehicle.position.x - position.x, vehicle.position.y - position.y);

		if (distance.length() < radius) {
			System.out.println("Collision between " + name + " & " + vehicle.getName());

			vehicle.setVelocity(distance);
			velocity.negate();
		}
	}

	/* ------------------------- */
	/* --- Drawing functions --- */
	/* ------------------------- */

	@Override
	public void draw(Graphics2D g2d) {

		/* The vehicle */
		g2d.setPaint(Color.blue);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		g2d.fillOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);

		/* Its sight rectangle*/
		g2d.setPaint(Color.orange);
		g2d.draw(sight);

		/* Its velocity */
		drawVector(g2d, velocity, Color.magenta, 20.0d);

		/* Its trajectory correction */
		drawVector(g2d, correction, Color.green, 1.0d);

		/* Its local space*/
		drawVector(g2d, forward, Color.cyan, 20.0d);
		drawVector(g2d, side, Color.cyan, 20.0d);

		/* Its future position and the corresponding point on the road */
		drawPoint(g2d, futurePosition, Color.orange, 8.0d);
		drawPoint(g2d, nearestPointOnRoad, Color.red, 8.0d);
		g2d.drawLine((int) futurePosition.x, (int) futurePosition.y, (int) nearestPointOnRoad.x, (int) nearestPointOnRoad.y);

		/* Its name */
		g2d.setPaint(Color.white);
		g2d.drawString(name, (float) (position.x + radius), (float) position.y);
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

	/**
	 * Draws a {@link Point2d}
	 * @param g2d
	 * @param p
	 * @param c
	 * @param radius
	 */
	private void drawPoint(Graphics2D g2d, Point2d p, Color c, double radius) {
		g2d.setPaint(c);
		g2d.fillOval((int) (p.x - (radius / 2.0d)), (int) (p.y - (radius / 2.0d)), (int) radius, (int) radius);
	}
}
