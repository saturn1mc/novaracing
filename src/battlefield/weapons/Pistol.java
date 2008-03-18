/**
 * 
 */
package battlefield.weapons;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Timer;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.BattleField;
import battlefield.bots.Bot;

/**
 * @author camille
 *
 */
public class Pistol extends Weapon {

	public static final String name = "Pistol";
	public static int MAX_AMMO = 10;
	public static int MAX_RANGE = 300;
	public static int FIRE_LATENCY = 1000;
	public static double FIRE_STRENGTH = 10d;

	private int bullets;
	private boolean cooling;

	public Pistol(Point2d position) {
		super(name, position);
		this.bullets = MAX_AMMO;
	}

	@Override
	public synchronized int ammoLeft() {
		return bullets;
	}
	
	@Override
	public int fireLatency() {
		return FIRE_LATENCY;
	}

	@Override
	public synchronized void shoot(BattleField env, LinkedList<Bot> enemies, Point2d origin, Vector2d direction) {

		if (canShoot()) {

			bullets--;
			
			direction.normalize();
			env.fireBullet(new Bullet(new Point2d(origin), enemies, direction, FIRE_STRENGTH, MAX_RANGE));
			
			setCooling(true);
			
			ActionListener al = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					setCooling(false);
				}
			};
			
			Timer timer = new Timer(FIRE_LATENCY, al);
			timer.setRepeats(false);
			timer.start();
		}
	}

	@Override
	public synchronized void reload(int bullets) {
		this.bullets += bullets;
		this.bullets = Math.max(this.bullets, MAX_AMMO);
	}
	
	private synchronized boolean canShoot() {
		return (bullets > 0 && !cooling);
	}
	
	private synchronized void setCooling(boolean cooling){
		this.cooling = cooling;
	}

	@Override
	public int maxRange() {
		return MAX_RANGE;
	}

	@Override
	public void draw(Graphics2D g2d) {
		//TODO
	}

	public int maxAmmo(){
		return MAX_AMMO;
	}
	
	@Override
	public void update(BattleField env) {
		// nothing...
	}
}
