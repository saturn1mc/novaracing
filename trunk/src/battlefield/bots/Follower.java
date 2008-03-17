/**
 * 
 */
package battlefield.bots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.BattleField;

/**
 * @author camille
 * 
 */
public class Follower extends Bot {

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
	private static final double maxForce = 0.2d;

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
	 * Current targets
	 */
	private Leader leader;
	private Point2d target;

	/**
	 * Damages
	 */
	private double damages;

	/**
	 * Bot's color
	 */
	private Color color;

	public Follower(String name, Point2d position, Color color) {
		super(name, position);

		this.color = color;

		this.forward = new Vector2d(0, 0);
		this.side = new Vector2d(0, 0);

		this.velocity = new Vector2d(0, 0);
		this.futurePosition = new Point2d(0, 0);
		this.leader = null;
		this.sight = new Polygon();

		this.steering = new Vector2d(0, 0);

		this.correction = new Vector2d(0, 0);
	}

	@Override
	public void update(BattleField env) {
		if (leader != null) {

			target = leader.getTargetFor(this);

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
			sight.addPoint((int) (position.x + (side.x * radius)), (int) (position.y + (side.y * radius)));
			sight.addPoint((int) (position.x - (side.x * radius)), (int) (position.y - (side.y * radius)));
			sight.addPoint((int) (futurePosition.x - (side.x * radius)), (int) (futurePosition.y - (side.y * radius)));
			sight.addPoint((int) (futurePosition.x + (side.x * radius)), (int) (futurePosition.y + (side.y * radius)));
		}
	}

	/**
	 * Computes the forces to apply to the vehicle (steering direction,
	 * acceleration and velocity)
	 */
	private void updateForces() {

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

		if (leader != null) {

			boolean avoiding = false;

			/*
			 * Correction to avoid exiting the area
			 */
			if (!env.getSurface().getArea().contains(futurePosition.x, futurePosition.y)) {
				
				Vector2d containmentCorrection = new Vector2d(0,0);
				
				if(futurePosition.x < 0){
					containmentCorrection.set(- futurePosition.x, containmentCorrection.y);
				}
				
				if(futurePosition.y < 0){
					containmentCorrection.set(containmentCorrection.x, -futurePosition.y);
				}
				
				if(futurePosition.x > env.getSurface().getArea().width){
					containmentCorrection.set(futurePosition.x - env.getSurface().getArea().width, containmentCorrection.y);
				}
				
				if(futurePosition.y > env.getSurface().getArea().height){
					containmentCorrection.set(containmentCorrection.x, futurePosition.y - env.getSurface().getArea().height);
				}
				
				correction.sub(containmentCorrection);
				
				avoiding = true;
			}

			/*
			 * Corrections to avoid obstacles and other vehicles
			 */
			if (!avoiding) {
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

						avoidanceCorrection.scale(intersection.getWidth() * radius * 2.0);
						correction.add(avoidanceCorrection);
					}
				}
			}

			/*
			 * Correction to stay on target (arrival)
			 */
			if (!avoiding) {

				speed = getArrivalSpeed();

				Vector2d targetCorrection = new Vector2d(target.x - futurePosition.x, target.y - futurePosition.y);
				correction.add(targetCorrection);
			}
		}
	}

	private double getArrivalSpeed() {
		Vector2d targetOffset = new Vector2d(target.x - position.x, target.y - position.y);
		double distance = targetOffset.length();

		double rampedSpeed = maxSpeed * (distance / (radius * 4.0));
		double clippedSpeed = Math.min(rampedSpeed, maxSpeed);

		if (clippedSpeed < 0.01) {
			clippedSpeed = 0;
		}

		return clippedSpeed;
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

	public Bot getCurrentTarget() {
		return leader;
	}

	public void setLeader(Leader leader) {

		this.leader = leader;

		this.steering = new Vector2d(leader.getPosition().x - position.x, leader.getPosition().y - position.y);
		steering.normalize();
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}

	@Override
	public double getRadius() {
		return radius;
	}

	/* ------------------------- */
	/* --- Drawing functions --- */
	/* ------------------------- */

	@Override
	public void draw(Graphics2D g2d) {

		/* The bot */
		g2d.setPaint(color);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		g2d.fillOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);

		if (Bot.showForces) {
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

			/* Its future position */
			drawPoint(g2d, futurePosition, Color.orange, 8.0d);

			/* Its name */
			g2d.setPaint(Color.white);
			g2d.drawString(name, (float) (position.x + radius), (float) position.y);
		}
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
