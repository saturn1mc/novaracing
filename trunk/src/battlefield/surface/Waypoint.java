/**
 * 
 */
package battlefield.surface;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.BattleField;
import battlefield.bots.Bot;

/**
 * @author camille
 * 
 */
public class Waypoint extends Element {

	private static int id = 0;
	private static final String DEFAULT_NAME = "waypoint_";

	/**
	 * Waypoints radius
	 */
	public static final double radius = 30.0d;

	/**
	 * Next waypoint (could be null)
	 */
	protected Waypoint next;

	/**
	 * Previous waypoint (could be null)
	 */
	protected Waypoint previous;

	public Waypoint(Point2d position) {
		super(DEFAULT_NAME + "" + id++, position);
	}
	
	/**
	 * Indicates if a bot as reached the {@link Waypoint}
	 * 
	 * @param vehicle
	 * @return <code>true</code> if the element as reached the
	 *         {@link Waypoint}, or else <code>false</code>
	 */
	public boolean isReachedBy(Bot element) {
		Vector2d dist = new Vector2d(position.x - element.getPosition().x, position.y - element.getPosition().y);
		return (dist.length() <= radius);
	}

	@Override
	public void draw(Graphics2D g2d) {
		g2d.setPaint(Color.magenta);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
	}
	
	@Override
	public void update(BattleField env) {
		// nothing
	}

	public Waypoint getNext() {
		return next;
	}

	public void setNext(Waypoint next) {
		this.next = next;
	}

	public Waypoint getPrevious() {
		return previous;
	}

	public void setPrevious(Waypoint previous) {
		this.previous = previous;
	}
	
	@Override
	public String toString() {
		return "Waypoint(" + name + ") : " + position.x + " - " + position.y;
	}
}
