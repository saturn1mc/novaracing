/**
 * 
 */
package battlefield.bonuses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;

import battlefield.bots.Bot;
import battlefield.surface.Waypoint;

/**
 * @author camille
 *
 */
public class AmmoPoint extends Waypoint {

	private static BufferedImage ammoImage;
	private int ammo;

	public AmmoPoint(Point2d position, int ammo) {
		super(position);
		this.ammo = ammo;

		if (ammoImage == null) {
			try {
				ammoImage = ImageIO.read(getClass().getResource("/images/ammo_crate.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void takeAmmo(Bot bot, int ammoWanted) {
		int ammoGiven = Math.min(ammo, ammoWanted);
		bot.getCurrentWeapon().reload(ammoGiven);
		ammo -= ammoGiven;

	}
	
	public boolean isEmpty(){
		return ammo <= 0;
	}

	@Override
	public void draw(Graphics2D g2d) {

		if (ammoImage != null) {
			g2d.drawImage(ammoImage, (int)(position.x - (radius/2.0d)), (int)(position.y - (radius / 2.0d)), (int)radius, (int)radius, null);
		} else {
			g2d.setColor(Color.magenta);
			g2d.setStroke(new BasicStroke(2.0f));
			super.draw(g2d);
			g2d.setStroke(new BasicStroke());
		}
	}

	public int getAmmo() {
		return ammo;
	}
}
