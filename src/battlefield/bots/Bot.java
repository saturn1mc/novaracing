/**
 * 
 */
package battlefield.bots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import battlefield.BattleField;
import battlefield.aStar.Path;
import battlefield.surface.Waypoint;
import battlefield.weapons.Bullet;
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

	/**
	 * The bot's current life
	 */
	protected double life;

	/**
	 * Bot's enemies
	 */
	protected LinkedList<Bot> enemies;

	/**
	 * Bot's current state
	 */
	protected int currentState;

	/**
	 * Bot's current path
	 */
	protected Path currentPath;

	/**
	 * Current target
	 */
	protected Waypoint target;

	/**
	 * The bot's bounding box
	 */
	protected Rectangle bbox;

	public Bot(String name, Point2d position) {
		super();
		this.name = name;
		this.position = position;
		this.life = 1.0;
		enemies = new LinkedList<Bot>();
	}

	/**
	 * Draws the bot
	 * 
	 * @param g2d
	 */
	public abstract void draw(Graphics2D g2d);

	/**
	 * Update the bot on each frame
	 * 
	 * @param env
	 */
	public abstract void update(BattleField env);

	protected void drawLifeBar(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(2.0f));

		if (life >= 0.5) {
			g2d.setColor(Color.green);
		} else if (life >= 0.2) {
			g2d.setColor(Color.orange);
		} else {
			g2d.setColor(Color.red);
		}

		g2d.drawLine((int) (position.x - (getRadius() / 2.0d)), (int) (position.y + getRadius()), (int) ((position.x - (getRadius() / 2.0d)) + (life * getRadius())), (int)(position.y + getRadius()));

		g2d.setStroke(new BasicStroke());
	}

	/**
	 * Search and return the first enemy at sight
	 * 
	 * @param env
	 *            the {@link BattleField}
	 * @return the first enemy at sight if any, or else <code>null</code>
	 */
	protected Bot enemyAtSight(BattleField env) {

		LinkedList<Bot> deadEnemies = new LinkedList<Bot>();

		for (Bot enemy : enemies) {

			if (!enemy.isAlive()) {
				deadEnemies.add(enemy);
			} else {
				if (env.getSurface().canSee(position, enemy.position)) {
					return enemy;
				}
			}
		}

		for (Bot enemy : deadEnemies) {
			enemies.remove(enemy);
		}

		return null;
	}

	/**
	 * Shoot a {@link Bullet} toward an enemy {@link Bot} with the current
	 * carried {@link Weapon}
	 * 
	 * @param env
	 *            the {@link BattleField}
	 * @param enemy
	 *            the enemy {@link Bot}
	 */
	protected void shootEnemy(BattleField env, Bot enemy) {
		Vector2d aim = new Vector2d(enemy.position.x - position.x, enemy.position.y - position.y);

		if (currentWeapon != null) {
			currentWeapon.shoot(env, enemies, new Point2d(position.x, position.y), aim);
		}
	}

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

	public void damage(double damage) {
		this.life -= damage;
	}

	public void heal(double life) {
		this.life += life;
	}

	public abstract double getRadius();

	public Rectangle getBBox() {
		return bbox;
	}

	public void addEnemies(LinkedList<Bot> enemies) {
		this.enemies.addAll(enemies);
	}

	public Weapon getCurrentWeapon() {
		return currentWeapon;
	}

	public void setCurrentWeapon(Weapon currentWeapon) {
		this.currentWeapon = currentWeapon;
	}

	public boolean isAlive() {
		return life > 0;
	}

	public double getLife() {
		return life;
	}
}
