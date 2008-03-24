/**
 * 
 */
package battlefield.bots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.LinkedList;

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
public class Leader extends Bot {

	public static final int STATE_SEARCHING = 0;
	public static final int STATE_ATTACKING = 1;
	public static final int STATE_ESCAPING = 2;
	public static final int STATE_RELOAD = 3;
	public static final int STATE_WOUNDED = 4;

	public static final int FORMATION_NONE = 0;
	public static final int FORMATION_LINE = 1;
	public static final int FORMATION_SQUARE = 2;
	public static final int FORMATION_WING = 3;
	public static final int FORMATION_SHIELD = 4;

	/**
	 * Current targeted enemy
	 */
	protected Bot enemy;

	/**
	 * Latest registered follower
	 */
	protected LinkedList<Follower> followers;

	/**
	 * Orders to be given by the leader
	 */
	protected int formationOrder;

	public Leader(String name, Point2d position, Color color, int formation) {
		super(name, position);

		this.bbox = new Rectangle((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);

		this.color = color;

		this.forward = new Vector2d(0, 0);
		this.side = new Vector2d(0, 0);

		this.velocity = new Vector2d(0, 0);
		this.futurePosition = new Point2d(0, 0);
		this.target = null;
		this.sight = new Polygon();

		this.steering = new Vector2d(1, 0);

		this.correction = new Vector2d(0, 0);

		this.enemies = new LinkedList<Bot>();
		this.followers = new LinkedList<Follower>();
		this.formationOrder = formation;

		this.currentState = STATE_SEARCHING;
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

		if (this.currentWeapon.ammoLeft() < Bot.AMMO_WARNING_LEVEL) {
			currentState = STATE_RELOAD;
			target = null;
		} else if (this.life < Bot.HEALTH_WARNING_LEVEL) {
			currentState = STATE_WOUNDED;
			target = null;
		}

		switch (currentState) {

		case STATE_SEARCHING:

			formationOrder = FORMATION_SQUARE;

			if (target == null) {

				while (currentPath == null || !currentPath.isSolved()) {

					Point2d start = env.getSurface().getGraph().getRandomPoint();
					Point2d stop = env.getSurface().getGraph().getRandomPoint();

					currentPath = new Path(env.getSurface().solve(new Point2d(start.x, start.y), new Point2d(stop.x, stop.y)));
				}

				setTarget(currentPath.getPoints().getFirst());

			} else {
				if (target.isReachedBy(this)) {
					target = target.getNext();
				}

				enemy = enemyAtSight(env);

				if (enemy != null) {

					if (getFollowersNumber() > 3) { // Panic limit

						currentState = STATE_ATTACKING;
						currentPath = null;

						target = new Waypoint(enemy.position);

					} else {
						currentState = STATE_ESCAPING;
						currentPath = null;
					}
				}
			}

			break;

		case STATE_ATTACKING:

			formationOrder = FORMATION_WING;
			enemy = enemyAtSight(env);

			if (enemy != null) {
				currentState = STATE_ATTACKING;
				target = new Waypoint(enemy.position);

				shootEnemy(env, enemy);

			} else {
				target = null;
				currentState = STATE_SEARCHING;
				updateState(env);
			}

			break;

		case STATE_ESCAPING:

			formationOrder = FORMATION_LINE;
			enemy = enemyAtSight(env);

			if (enemy != null) {
				currentState = STATE_ESCAPING;

				Vector2d distance = new Vector2d(position.x - enemy.position.x, position.y - enemy.position.y);

				target = new Waypoint(new Point2d(position.x + distance.x, position.y + distance.y));

				shootEnemy(env, enemy);

			} else {
				target = null;
				currentState = STATE_SEARCHING;
				updateState(env);
			}

			break;

		case STATE_RELOAD:

			formationOrder = FORMATION_SHIELD;
			enemy = enemyAtSight(env);

			if (enemy != null) {
				shootEnemy(env, enemy);
			}

			if (target == null) { // First time in reload state
				target = env.nearestAmmoPoint(this);
			}

			if (target != null && target.isReachedBy(this)) {
				AmmoPoint pt = (AmmoPoint) target;
				pt.takeAmmo(this, this.getCurrentWeapon().maxAmmo());
				currentState = STATE_SEARCHING;
				updateState(env);
			}

			break;

		case STATE_WOUNDED:

			formationOrder = FORMATION_SHIELD;
			enemy = enemyAtSight(env);

			if (enemy != null) {
				shootEnemy(env, enemy);
			}

			if (target == null) { // First time in state reloading
				target = env.nearestLifePoint(this);
			}

			if (target != null && target.isReachedBy(this)) {
				LifePoint pt = (LifePoint) target;
				pt.takeLife(this, 1 - this.getLife());
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
	protected void updateForces() {

		/* Going to target */
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
	protected void updateFuturePosition() {
		futurePosition.set(position.x + (velocity.x * predictionCoeff), position.y + (velocity.y * predictionCoeff));
	}

	/**
	 * Computes the correction to apply to the current steering
	 * 
	 * @param env
	 */
	protected void computeTrajectoryCorrection(BattleField env) {

		correction.set(0, 0);

		if (target != null) {

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

	public Vector2d getSpeed() {
		return steering;
	}

	public void setSteering(Vector2d speed) {
		this.steering = speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Bot getEnemy() {
		return enemy;
	}

	public void setEnemy(Bot enemy) {
		this.enemy = enemy;
	}

	public Waypoint getCurrentTarget() {
		return target;
	}

	public void setTarget(Waypoint target) {

		this.target = target;

		this.steering = new Vector2d(target.getPosition().x - position.x, target.getPosition().y - position.y);
		steering.normalize();
	}

	public void setFormationOrder(int order) {
		this.formationOrder = order;
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

	public synchronized Waypoint getTargetFor(Follower f) {

		Point2d target;
		Point2d reference;
		int id = getFollowerId(f);
		int followersNumber = getFollowersNumber();

		switch (formationOrder) {

		case FORMATION_NONE:
			target = this.position;
			break;

		case FORMATION_SQUARE:
			int squareSize = (int) Math.sqrt(followersNumber);

			int column = (id - 1) % squareSize;
			int line = (id - 1) / squareSize;

			reference = new Point2d(this.position.x + (this.forward.x * (-radius * 2.0)) + (this.side.x * -(radius * 2.0) * (squareSize / 2)), this.position.y + (this.forward.y * -(radius * 2.0)) + (this.side.y * -(radius * 2.0) * (squareSize / 2)));
			target = new Point2d(reference.x + (this.side.x * (radius * 2.0 * column)) + (this.forward.x * -(radius * 2.0 * line)), reference.y + (this.side.y * (radius * 2.0 * column)) + (this.forward.y * -(radius * 2.0 * line)));

			break;

		case FORMATION_LINE:
			target = new Point2d(this.position.x + (this.forward.x * -(radius * 2.0 * id)), this.position.y + (this.forward.y * -(radius * 2.0 * id)));
			break;

		case FORMATION_WING:

			int leftWingLimit = (followersNumber / 2);
			int dist = (id % (followersNumber - leftWingLimit));

			reference = new Point2d(this.position.x - (forward.x * 2.0 * radius), this.position.y - (forward.y * 2.0 * radius));

			if (id <= leftWingLimit) {
				Vector2d leftWingDir = new Vector2d((forward.x * Math.cos(Math.PI)) - (forward.y * Math.sin(Math.PI * 0.9d)), (forward.y * Math.cos(Math.PI * 0.9d)) + (forward.x * Math.sin(Math.PI * 0.9d)));
				leftWingDir.normalize();

				target = new Point2d(reference.x + (leftWingDir.x * (dist * radius * 2.0)), reference.y + (leftWingDir.y * (dist * radius * 2.0)));
			} else {
				Vector2d rightWingDir = new Vector2d((forward.x * Math.cos(-Math.PI * 0.9d)) - (forward.y * Math.sin(-Math.PI * 0.9d)), (forward.y * Math.cos(-Math.PI * 0.9d)) + (forward.x * Math.sin(-Math.PI * 0.9d)));
				rightWingDir.normalize();

				target = new Point2d(reference.x + (rightWingDir.x * (dist * radius * 2.0)), reference.y + (rightWingDir.y * (dist * radius * 2.0)));
			}

			break;

		case FORMATION_SHIELD:

			double shieldRadius = (followersNumber * 2.0 * radius) / (2.0 * Math.PI);
			double rotation = (id - 1) * ((2.0d * Math.PI) / followersNumber);
			Vector2d dir = new Vector2d((forward.x * Math.cos(rotation)) - (forward.y * Math.sin(rotation)), (forward.y * Math.cos(rotation)) + (forward.x * Math.sin(rotation)));

			target = new Point2d(this.position.x + (dir.x * shieldRadius), this.position.y + (dir.y * shieldRadius));

			break;

		default:
			target = this.position;
			break;
		}

		return new Waypoint(new Point2d(target.x, target.y));
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

		/* Leader's aura */
		g2d.setPaint(Color.white);
		g2d.fillOval((int) (position.x - ((radius + 4) / 2.0d)), (int) (position.y - ((radius + 4) / 2.0d)), (int) (radius + 4), (int) (radius + 4));
		
		/* The bot */
		if (logo != null) {
			g2d.drawImage(logo, (int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius, null);
		} else {
			g2d.setPaint(color);
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

			/* Its current path */
			if (currentPath != null && currentPath.isSolved()) {
				Waypoint prev = null;

				for (Waypoint wp : currentPath.getPoints()) {

					wp.draw(g2d);

					if (prev != null) {
						g2d.drawLine((int) prev.getPosition().x, (int) prev.getPosition().y, (int) wp.getPosition().x, (int) wp.getPosition().y);
					}

					prev = wp;
				}
			}

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
