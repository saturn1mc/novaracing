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
import battlefield.aStar.Path;
import battlefield.bonuses.AmmoPoint;
import battlefield.bonuses.LifePoint;
import battlefield.surface.Waypoint;

/**
 * @author camille
 * 
 */
public class Follower extends Bot {

	public static final int STATE_SEARCHING = 0;
	public static final int STATE_LEADED = 1;
	public static final int STATE_RELOAD = 2;
	public static final int STATE_WOUNDED = 3;

	/**
	 * Current targets
	 */
	private Leader leader;

	/**
	 * Bot's color
	 */
	private Color color;

	public Follower(String name, Point2d position, Color color) {
		super(name, position);

		this.bbox = new Rectangle((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);

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

		updateState(env);

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
			sight.addPoint((int) (position.x + (side.x * radius)), (int) (position.y + (side.y * radius)));
			sight.addPoint((int) (position.x - (side.x * radius)), (int) (position.y - (side.y * radius)));
			sight.addPoint((int) (futurePosition.x - (side.x * radius)), (int) (futurePosition.y - (side.y * radius)));
			sight.addPoint((int) (futurePosition.x + (side.x * radius)), (int) (futurePosition.y + (side.y * radius)));
		}

		this.bbox.setLocation((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)));
	}

	private void updateState(BattleField env) {

		Bot enemy;

		if (this.currentWeapon.ammoLeft() < Bot.AMMO_WARNING_LEVEL) {
			currentState = STATE_RELOAD;
			target = null;
		} else if (this.life < Bot.HEALTH_WARNING_LEVEL) {
			currentState = STATE_WOUNDED;
			target = null;
		}

		switch (currentState) {
		case STATE_SEARCHING:

			if (leader != null && leader.isAlive()) {
				currentState = STATE_LEADED;
				updateState(env);
			}

			if (target == null) {

				while (currentPath == null || !currentPath.isSolved()) {

					Point2d start = env.getSurface().getGraph().getRandomPoint();
					Point2d stop = env.getSurface().getGraph().getRandomPoint();

					currentPath = new Path(env.getSurface().solve(new Point2d(start.x, start.y), new Point2d(stop.x, stop.y)));
				}

				target = currentPath.getPoints().getFirst();

			} else {
				if (target.isReachedBy(this)) {
					target = target.getNext();
				}
			}

			enemy = enemyAtSight(env);

			if (enemy != null) {
				target = new Waypoint(new Point2d(enemy.position.x + (3.0d * radius), enemy.position.y + (3.0d * radius)));
				shootEnemy(env, enemy);
			}

			break;

		case STATE_LEADED:
			if (leader != null && leader.isAlive()) {
				target = leader.getTargetFor(this);
				if (leader.getEnemy() != null) {
					shootEnemy(env, leader.getEnemy());
				}
			} else {
				currentState = STATE_SEARCHING;
				updateState(env);
			}
			break;

		case STATE_RELOAD:

			enemy = enemyAtSight(env);

			if (enemy != null) {
				shootEnemy(env, enemy);
			}

			if (target == null) { // First time in reload state
				target = env.nearestAmmoPoint(this);
			}

			if (target != null && target.isReachedBy(this)) {
				AmmoPoint ap = (AmmoPoint) target;
				ap.takeAmmo(this, this.getCurrentWeapon().maxAmmo());
				currentState = STATE_SEARCHING;
				updateState(env);
			}

			break;

		case STATE_WOUNDED:

			enemy = enemyAtSight(env);

			if (enemy != null) {
				shootEnemy(env, enemy);
			}

			if (target == null) { // First time in state reloading
				target = env.nearestLifePoint(this);
			}

			if (target != null && target.isReachedBy(this)) {
				LifePoint lp = (LifePoint) target;
				lp.takeLife(this, 1 - this.getLife());
				currentState = STATE_SEARCHING;
				updateState(env);
			}

			break;

		default:
			System.err.println("Unknown state : " + currentState);
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

				Vector2d containmentCorrection = new Vector2d(0, 0);

				if (futurePosition.x < (2.0d * radius)) {
					containmentCorrection.set(futurePosition.x - (2.0d * radius), containmentCorrection.y);
				}

				if (futurePosition.y < (2.0d * radius)) {
					containmentCorrection.set(containmentCorrection.x, futurePosition.y - (2.0d * radius));
				}

				if (futurePosition.x > env.getSurface().getArea().width - (2.0d * radius)) {
					containmentCorrection.set(futurePosition.x - env.getSurface().getArea().width + (2.0d * radius), containmentCorrection.y);
				}

				if (futurePosition.y > env.getSurface().getArea().height - (2.0d * radius)) {
					containmentCorrection.set(containmentCorrection.x, futurePosition.y - env.getSurface().getArea().height + (2.0d * radius));
				}

				correction.sub(containmentCorrection);

				avoiding = true;
			}

			/*
			 * Corrections to avoid obstacles
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

				Vector2d targetCorrection = new Vector2d(target.getPosition().x - futurePosition.x, target.getPosition().y - futurePosition.y);
				correction.add(targetCorrection);
			}
		}
	}

	private double getArrivalSpeed() {
		Vector2d targetOffset = new Vector2d(target.getPosition().x - position.x, target.getPosition().y - position.y);
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

	@Override
	public void draw(Graphics2D g2d) {

		/* The bot */
		if (logo != null) {
			g2d.drawImage(logo, (int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius, null);
		} else {
			g2d.setPaint(color);
			g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
			g2d.fillOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		}

		/* Its life bar */
		drawLifeBar(g2d);

		/* Show warnings (life is more important than ammo) */
		if (life < HEALTH_WARNING_LEVEL) {
			drawWarning(g2d, Bot.WARNING_HEALTH);
		} else {
			if (currentWeapon.ammoLeft() < AMMO_WARNING_LEVEL) {
				drawWarning(g2d, Bot.WARNING_AMMO);
			}
		}

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
}
