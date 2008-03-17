/**
 * 
 */
package battlefield.weapons;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.BattleField;
import battlefield.surface.Element;

/**
 * @author camille
 *
 */
public abstract class Weapon extends Element{
	
	public Weapon(String name, Point2d position) {
		super(name, position);
	}
	
	public abstract int maxRange();
	public abstract int fireRate();
	public abstract int ammoLeft();
	public abstract void fire(BattleField env, Point2d origin, Vector2d direction);
}
