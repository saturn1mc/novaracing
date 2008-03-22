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
public class LifePoint extends Waypoint{
	
	private double life;
	
	public LifePoint(Point2d position, int life) {
		super(position);
		this.life = life;
	}
	
	public synchronized void takeLife(Bot bot, double lifeWanted){
		double lifeGiven = Math.min(life, lifeWanted);
		bot.heal(lifeGiven);
		life -= lifeGiven;		
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.cyan);
		g2d.setStroke(new BasicStroke(2.0f));
		super.draw(g2d);
		g2d.setStroke(new BasicStroke());
	}
	
	public double getLife() {
		return life;
	}
}
