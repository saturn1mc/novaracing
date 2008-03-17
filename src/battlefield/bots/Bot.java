/**
 * 
 */
package battlefield.bots;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;

import battlefield.BattleField;


import elements.HumanVehicle;
import elements.Vehicle;

/**
 * @author camille
 *
 */
public abstract class Bot {
	
	public static final boolean showForces = false;
	
	/**
	 * The bot's name (could be used as an identifier)
	 */
	protected String name;
	
	protected double life;
	protected int ammo;
	
	/**
	 * The bot's absolute position in the window
	 */
	protected Point2d position;
	
	public Bot(String name, Point2d position) {
		super();
		this.name = name;
		this.position = position;
	}

	/**
	 * Draws the bot
	 * @param g2d
	 */
	public void draw(Graphics2D g2d){}
	
	/**
	 * Update the bot on each frame
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
	 * @return <code>true</code> if the bot must be avoided or else false
	 */
	public boolean avoidedBy(Vehicle vehicle){return false;}
	
	/**
	 * Avoidance indicator for a {@link HumanVehicle}
	 * @param vehicle
	 * @return <code>true</code> if the bot must be avoided or else false
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
