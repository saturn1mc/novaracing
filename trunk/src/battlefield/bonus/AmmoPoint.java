/**
 * 
 */
package battlefield.bonus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;

import battlefield.bots.Bot;
import battlefield.surface.Waypoint;

/**
 * @author camille
 *
 */
public class AmmoPoint extends Waypoint{

	private int ammo;
	
	public AmmoPoint(Point2d position, int ammo) {
		super(position);
		this.ammo = ammo;
	}
	
	public synchronized void takeAmmo(Bot bot, int ammoWanted){
		int ammoGiven = Math.min(ammo, ammoWanted);
		bot.getCurrentWeapon().reload(ammoGiven);
		ammo -= ammoGiven;
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.green);
		g2d.setStroke(new BasicStroke(2.0f));
		super.draw(g2d);
		g2d.setStroke(new BasicStroke());
	}
	
	public int getAmmo() {
		return ammo;
	}
}
