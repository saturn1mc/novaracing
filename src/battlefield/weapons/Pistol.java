/**
 * 
 */
package battlefield.weapons;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.BattleField;

/**
 * @author camille
 *
 */
public class Pistol extends Weapon {

	public static final String name = "Pistol";
	public static int MAX_AMMO = 15;
	public static int MAX_RANGE = 200;
	public static int FIRE_RATE = 1;
	
	private int bullets;
	
	public Pistol(Point2d position) {
		super(name, position);
		this.bullets = MAX_AMMO;
	}
	
	@Override
	public int ammoLeft() {
		return bullets;
	}

	@Override
	public int fireRate() {
		return FIRE_RATE;
	}

	@Override
	public void fire(BattleField env, Point2d origin, Vector2d direction) {
		
	}

	@Override
	public int maxRange() {
		return MAX_RANGE;
	}

	@Override
	public void draw(Graphics2D g2d) {
		//TODO
	}

	@Override
	public void update(BattleField env) {
		// nothing...
	}
}
