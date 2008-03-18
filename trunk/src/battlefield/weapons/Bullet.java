/**
 * 
 */
package battlefield.weapons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
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
public class Bullet extends Element{

	public static double radius = 4.0;
	
	private Vector2d dir;
	private double maxRange;
	private double range;
	private boolean hasHit;
	private LinkedList<Bot> targets;
	
	public Bullet(Point2d position, LinkedList<Bot> enemies, Vector2d dir, double velocity, double maxRange) {
		super("Bullet", position);
		
		this.dir = dir;
		this.dir.scale(velocity);
		
		this.targets = enemies;
		
		this.maxRange = maxRange;
		this.range = 0;
		
		hasHit = false;
	}
	
	public void hitTest(BattleField env){
		
		for(Bot bot : targets){
			Vector2d trajectory = new Vector2d(dir.x, dir.y);
			double dist = trajectory.length();
			trajectory.normalize();
			
			for (int i = 1; i < dist + 1; i++) {
				Point2d target = new Point2d(position.x + (trajectory.x * i), position.y + (trajectory.y * i));			
				
				for(Polygon p : env.getSurface().getObjects()){
					if(p.contains(target.x, target.y)){
						hasHit = true;
						return;
					}
				}
				
				if (bot.getBBox().contains(target.x, target.y)) {
					hasHit = true;
					bot.damage(0.1);
					return;
				}
			}
		}
	}
	
	public boolean isFlying(){
		return ((range <= maxRange) && (!hasHit));
	}
	
	@Override
	public void update(BattleField env) {
		range += dir.length();
		this.position.add(dir);
	}

	@Override
	public void draw(Graphics2D g2d) {
		g2d.setPaint(Color.black);
		g2d.drawOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
		g2d.fillOval((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)), (int) radius, (int) radius);
	}
}
