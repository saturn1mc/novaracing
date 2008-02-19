/**
 * 
 */
package elements;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import environment.Environment;

/**
 * @author camille
 * 
 */
public class Vehicle extends Element {

	public static final double radius = 15.0d;
	protected static final double mass = 1.0d;
	protected static final double maxSpeed = 1.0d;
	protected static final double maxForce = 0.05d;
	protected static final double predictionCoeff = 50.0d;

	protected double damages;

	protected Vector2d steering;
	protected Vector2d correction;
	protected Vector2d velocity;
	protected Point2d futurePosition;

	protected Point2d nearestPointOnRoad;

	protected Waypoint target;

	public Vehicle(String name, Point2d position, Waypoint target) {
		super(name, position);
		
		this.velocity = new Vector2d(0, 0);
		this.futurePosition = new Point2d(0, 0);
		this.target = target;
		
		this.steering = new Vector2d(target.getPosition().x - position.x, target.getPosition().y - position.y);
		
		this.correction = new Vector2d(0, 0);
	}

	@Override
	public void draw(Graphics2D g2d) {
		g2d.setPaint(Color.BLUE);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		g2d.fillOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		drawVector(g2d, velocity, Color.magenta);
		drawPoint(g2d, futurePosition, Color.orange);

		drawPoint(g2d, nearestPointOnRoad, Color.red);
	}

	private void drawVector(Graphics2D g2d, Vector2d v, Color c) {
		g2d.setPaint(c);
		g2d.drawLine((int) position.x, (int) position.y, (int) (position.x + v.x), (int) (position.y + v.y));
		g2d.drawOval((int) (position.x + v.x - 2.5), (int) (position.y + v.y - 2.5), 5, 5);
	}

	private void drawPoint(Graphics2D g2d, Point2d p, Color c) {
		g2d.setPaint(c);
		g2d.drawOval((int) (p.x - 2.5), (int) (p.y - 2.5), 5, 5);
	}

	@Override
	public void update(Environment env) {
		updateForces();
		updateFuturePosition();
		computeTrajectoryCorrection();
	}
	
	private void updateForces() {
		if (target.isReachedBy(this)) {
			this.target = target.getNext();
		}
		
		steering.normalize();
		
		steering.add(truncate(correction, maxForce));
		
		Vector2d steeringForce = new Vector2d(truncate(steering, maxForce));
		Vector2d acceleration = new Vector2d(steeringForce);
		acceleration.scale(1.0d / mass);
		
		Vector2d speed = new Vector2d(velocity);
		speed.add(acceleration);
		
		velocity.set(truncate(speed, maxSpeed));
		
		position.add(velocity);
	}

	private void updateFuturePosition() {
		futurePosition.set(position.x + (velocity.x * predictionCoeff), position.y + (velocity.y * predictionCoeff));
	}

	private void computeTrajectoryCorrection() {
		nearestPointOnRoad = target.nearestPointOnRoad(futurePosition);

		correction.set(nearestPointOnRoad.x - futurePosition.x, nearestPointOnRoad.y - futurePosition.y);

		if (correction.length() < Waypoint.radius) {
			correction.set(0, 0);
		}
	}
	
	private Vector2d truncate(Vector2d v, double max){
		
		Vector2d vT = new Vector2d(v);
		double l = vT.length();
		
		if(l > max){
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

	public void setSpeed(Vector2d speed) {
		this.steering = speed;
	}

	public Waypoint getCurrentWayPoint() {
		return target;
	}

	public void setCurrentWayPoint(Waypoint currentWayPoint) {
		this.target = currentWayPoint;
	}
}
