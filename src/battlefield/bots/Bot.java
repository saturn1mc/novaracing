/**
 * 
 */
package battlefield.bots;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;

import battlefield.BattleField;
import battlefield.weapons.Weapon;



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
	
	/**
	 * Bot's current weapon
	 */
	protected Weapon currentWeapon;
	
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
	public abstract void draw(Graphics2D g2d);
	
	/**
	 * Update the bot on each frame
	 * @param env
	 */
	public abstract void update(BattleField env);

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
	
	public void setCurrentWeapon(Weapon currentWeapon) {
		this.currentWeapon = currentWeapon;
	}
}
