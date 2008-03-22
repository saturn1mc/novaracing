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
public class LifePoint extends Waypoint{
	
	private static BufferedImage lifeImage;
	private double life;
	
	public LifePoint(Point2d position, int life) {
		super(position);
		this.life = life;
		
		if (lifeImage == null) {
			try {
				lifeImage = ImageIO.read(getClass().getResource("/images/heart.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void takeLife(Bot bot, double lifeWanted){
		double lifeGiven = Math.min(life, lifeWanted);
		bot.heal(lifeGiven);
		life -= lifeGiven;		
	}
	
	public boolean isEmpty(){
		return life <= 0;
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		if (lifeImage != null) {
			g2d.drawImage(lifeImage, (int)(position.x - (radius/2.0d)), (int)(position.y - (radius / 2.0d)), (int)radius, (int)radius, null);
		} else {
			g2d.setColor(Color.cyan);
			g2d.setStroke(new BasicStroke(2.0f));
			super.draw(g2d);
			g2d.setStroke(new BasicStroke());
		}
	}
	
	public double getLife() {
		return life;
	}
}
