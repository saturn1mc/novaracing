/**
 * 
 */
package battlefield.weapons;

import java.util.LinkedList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.BattleField;
import battlefield.bots.Bot;
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
	public abstract int fireLatency();
	public abstract int ammoLeft();
	public abstract void shoot(BattleField env, LinkedList<Bot> enemies, Point2d origin, Vector2d direction);
	public abstract void reload(int bullets);
}
