/**
 * 
 */
package bots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.LinkedList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import applets.BattleField;
import elements.HumanVehicle;
import elements.Vehicle;
import elements.Waypoint;

/**
 * @author camille
 * 
 */
public class Leader extends Bot {

	public static final int FORMATION_NONE = 0;
	public static final int FORMATION_LINE = 1;
	public static final int FORMATION_SQUARE = 2;

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
	 * Vehicle's sight
	 */
	private Polygon sight;

	/**
	 * Current target
	 */
	private Waypoint target;

	/**
	 * Latest registered follower
	 */
	private LinkedList<Follower> followers;

	private int formation;

	/**
	 * Damages
	 */
	private double damages;

	/**
	 * Current bonus element
	 */
	// private Element bonus;
	public Leader(String name, Point2d position, int formation) {
		super(name, position);

		this.forward = new Vector2d(0, 0);
		this.side = new Vector2d(0, 0);

		this.velocity = new Vector2d(0, 0);
		this.futurePosition = new Point2d(0, 0);
		this.target = null;
		this.sight = new Polygon();

		this.steering = new Vector2d(0, 0);

		this.correction = new Vector2d(0, 0);

		this.followers = new LinkedList<Follower>();
		this.formation = formation;
	}

	@Override
	public void update(BattleField env) {

		if (target != null) {

			// TODO take the other elements effects into account
			// (collision, bonuses, obstacles, ...)
			updateForces();

			updateFuturePosition();
			computeTrajectoryCorrection(env);

			forward.set(velocity);
			forward.normalize();

			side.set((forward.x * Math.cos(Math.PI / 2.0d)) - (forward.y * Math.sin(Math.PI / 2.0d)), (forward.y * Math.cos(Math.PI / 2.0d)) + (forward.x * Math.sin(Math.PI / 2.0d)));
			side.normalize();

			/* Updating position */
			position.add(velocity);

			/* Updating sight */
			sight.reset();
			sight.addPoint((int) (position.x + (side.x * (radius / 2.0d))), (int) (position.y + (side.y * (radius / 2.0d))));
			sight.addPoint((int) (position.x - (side.x * (radius / 2.0d))), (int) (position.y - (side.y * (radius / 2.0d))));
			sight.addPoint((int) (futurePosition.x - (side.x * (radius / 2.0d))), (int) (futurePosition.y - (side.y * (radius / 2.0d))));
			sight.addPoint((int) (futurePosition.x + (side.x * (radius / 2.0d))), (int) (futurePosition.y + (side.y * (radius / 2.0d))));

			if (target.isReachedBy(this)) {
				this.target = target.getNext();
			}
		}
	}

	/**
	 * Computes the forces to apply to the vehicle (steering direction,
	 * acceleration and velocity)
	 */
	private void updateForces() {

		/* Changing target */
		if (target != null) {

			speed = maxSpeed;

		} else {
			speed = 0;
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
	 * Updates the future position point (the position of the vehicle in
	 * <code>predictionCoeff</code> frames if it stays on the same direction)
	 */
	private void updateFuturePosition() {
		futurePosition.set(position.x + (velocity.x * predictionCoeff), position.y + (velocity.y * predictionCoeff));
	}

	/**
	 * Computes the correction to apply to the current steering
	 * 
	 * @param env
	 */
	private void computeTrajectoryCorrection(BattleField env) {

		correction.set(0, 0);

		if (target != null) {

			boolean avoiding = false;

			/*
			 * Corrections to avoid obstacles and other vehicles
			 */
			for (Polygon p : env.getSurface().getObjects()) {

				Rectangle bbox = p.getBounds();
				Rectangle intersection = bbox.intersection(sight.getBounds());

				/* If the element is on the way */
				if (!intersection.isEmpty()) {

					avoiding = true;

					Point2d A = position;
					Point2d B = futurePosition;

					double L = Math.sqrt(((B.x - A.x) * (B.x - A.x)) + ((B.y - A.y) * (B.y - A.y)));
					double S = (((A.y - bbox.getCenterY()) * (B.x - A.x)) - ((A.x - bbox.getCenterX()) * (B.y - A.y))) / (L * L);

					Vector2d avoidanceCorrection;

					if (S > 0) {
						avoidanceCorrection = new Vector2d(side.x, side.y);
					} else {
						avoidanceCorrection = new Vector2d(-side.x, -side.y);
					}

					avoidanceCorrection.scale(intersection.getWidth() * 2);
					correction.add(avoidanceCorrection);
				}
			}

			/*
			 * Correction to stay on target
			 */
			if (!avoiding) {
				Vector2d targetCorrection = new Vector2d(target.getPosition().x - futurePosition.x, target.getPosition().y - futurePosition.y);
				correction.add(targetCorrection);
			}
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

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Waypoint getCurrentTarget() {
		return target;
	}

	public void setTarget(Waypoint target) {

		this.target = target;

		this.steering = new Vector2d(target.getPosition().x - position.x, target.getPosition().y - position.y);
		steering.normalize();
	}
	
	public void setFormation(int formation){
		this.formation = formation;
	}

	public synchronized void registerFollower(Follower f) {
		followers.add(f);
	}

	public synchronized int getFollowerId(Follower f) {
		return followers.indexOf(f) + 1;
	}

	public synchronized int getFollowersNumber() {
		return followers.size();
	}

	public synchronized Point2d getTargetFor(Follower f) {

		Point2d target;

		switch (formation) {
		case FORMATION_NONE:
			target = this.position;
			break;

		case FORMATION_SQUARE:
			int squareSize = (int)Math.sqrt(getFollowersNumber());
			
			int id = getFollowerId(f);
			int column = (id-1) % squareSize;
			int line = (id-1) / squareSize;
			
			Point2d reference = new Point2d(this.position.x + (this.forward.x * (-radius * 2.0)) + (this.side.x * -(radius * 2.0) * (squareSize/2)), this.position.y + (this.forward.y * -(radius * 2.0)) + (this.side.y * -(radius * 2.0) * (squareSize/2)));
			target = new Point2d(reference.x + (this.side.x * (radius * 2.0 * column)) + (this.forward.x * -(radius * 2.0 * column)), reference.y + (this.side.y * (radius * 2.0 * line)) + (this.forward.y * -(radius * 2.0 * line)));
			
			break;

		case FORMATION_LINE:
			target = new Point2d(this.position.x + (this.forward.x * -(radius * 2.0 * getFollowerId(f))), this.position.y + (this.forward.y * -(radius * 2.0 * getFollowerId(f))));
			break;

		default:
			target = this.position;
			break;
		}

		return target;
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}

	@Override
	public double getRadius() {
		return radius;
	}

	/* ------------------------- */
	/* --- Avoidance --- */
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
	/* --- Drawing functions --- */
	/* ------------------------- */

	@Override
	public void draw(Graphics2D g2d) {

		/* The vehicle */
		g2d.setPaint(Color.red);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		g2d.fillOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);

		/* Its sight rectangle */
		g2d.setPaint(Color.orange);
		g2d.draw(sight);

		/* Its velocity */
		drawVector(g2d, velocity, Color.magenta, 20.0d);

		/* Its trajectory correction */
		drawVector(g2d, correction, Color.green, 1.0d);

		/* Its local space */
		drawVector(g2d, forward, Color.cyan, 20.0d);
		drawVector(g2d, side, Color.cyan, 20.0d);

		/* Its future position and the corresponding point on the road */
		drawPoint(g2d, futurePosition, Color.orange, 8.0d);

		/* Its name */
		g2d.setPaint(Color.white);
		g2d.drawString(name, (float) (position.x + radius), (float) position.y);
	}

	/**
	 * Draws a scaled {@link Vector2d}
	 * 
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
	 * 
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
