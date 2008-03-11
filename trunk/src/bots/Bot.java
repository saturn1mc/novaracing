/**
 * 
 */
package bots;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;

import applets.BattleField;

import elements.HumanVehicle;
import elements.Vehicle;

/**
 * @author camille
 *
 */
public abstract class Bot {
	
	/**
	 * The element's name (could be used as an identifier)
	 */
	protected String name;
	
	/**
	 * The element's absolute position in the window
	 */
	protected Point2d position;
	
	public Bot(String name, Point2d position) {
		super();
		this.name = name;
		this.position = position;
	}

	/**
	 * Draws the element
	 * @param g2d
	 */
	public void draw(Graphics2D g2d){}
	
	/**
	 * Update the element on each frame
	 * @param env
	 */
	public void update(BattleField env){}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Point2d getPosition() {
		return position;
	}

	public void setPosition(Point2d position) {
		this.position = position;
	}
	
	public double getRadius(){return 0.0d;}
	
	/**
	 * Avoidance indicator for a {@link Vehicle}
	 * @param vehicle
	 * @return <code>true</code> if the element must be avoided or else false
	 */
	public boolean avoidedBy(Vehicle vehicle){return false;}
	
	/**
	 * Avoidance indicator for a {@link HumanVehicle}
	 * @param vehicle
	 * @return <code>true</code> if the element must be avoided or else false
	 */
	public boolean avoidedBy(HumanVehicle vehicle){return false;}
	
	/**
	 * Effect on {@link Vehicle}.
	 * @param vehicle
	 */
	public void effectOn(Vehicle vehicle){}
	
	/**
	 * Effect on {@link HumanVehicle}
	 * @param vehicle
	 */
	public void effectOn(HumanVehicle vehicle){}
}
