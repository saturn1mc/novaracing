/**
 * 
 */
package battlefield.bots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
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

	public static final int WARNING_HEALTH = 0;
	public static final int WARNING_AMMO = 1;

	public static final int AMMO_WARNING_LEVEL = 5;
	public static final double HEALTH_WARNING_LEVEL = 0.7d;

	private static Image healingImage;
	private static Image ammoImage;

	/**
	 * The bot's name (could be used as an identifier)
	 */
	protected String name;

	/**
	 * Bot's radius
	 */
	protected static final double radius = 25.0d;

	/**
	 * Bot's mass
	 */
	protected static final double mass = 0.9d;

	/**
	 * Bot's maximum speed
	 */
	protected static final double maxSpeed = 1.1d;

	/**
	 * Bot's current speed
	 */
	protected double speed = 0.0d;

	/**
	 * Maximum force that can be applied to the vehicle
	 */
	protected static final double maxForce = 0.3d;

	/**
	 * Number of frame(s) to anticipate the movement
	 */
	protected static final double predictionCoeff = 30.0d;

	/**
	 * Steering force
	 */
	protected Vector2d steering;

	/**
	 * Current correction
	 */
	protected Vector2d correction;

	/**
	 * Velocity force
	 */
	protected Vector2d velocity;

	/**
	 * Local space X axis
	 */
	protected Vector2d forward;

	/**
	 * Local space Y axis
	 */
	protected Vector2d side;

	/**
	 * Predicted position
	 */
	protected Point2d futurePosition;

	/**
	 * Bot's sight
	 */
	protected Polygon sight;

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
	
	/**
	 * Bot's color
	 */
	protected Color color;
	
	/**
	 * Bot's logo
	 */
	protected Image logo;

	public Bot(String name, Point2d position) {
		super();
		this.name = name;
		this.position = position;
		this.life = 1.0;
		enemies = new LinkedList<Bot>();

		if (ammoImage == null) {
			try {
				ammoImage = ImageIO.read(getClass().getResource("/images/gun.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (healingImage == null) {
			try {
				healingImage = ImageIO.read(getClass().getResource("/images/redcross.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update the bot on each frame
	 * 
	 * @param env
	 */
	public abstract void update(BattleField env);

	/**
	 * Draws the bot
	 * 
	 * @param g2d
	 */
	public abstract void draw(Graphics2D g2d);

	/**
	 * Draws a scaled {@link Vector2d}
	 * 
	 * @param g2d
	 * @param v
	 * @param c
	 * @param scale
	 */
	protected void drawVector(Graphics2D g2d, Vector2d v, Color c, double scale) {
		g2d.setPaint(c);
		g2d.drawLine((int) position.x, (int) position.y, (int) (position.x + (v.x * scale)), (int) (position.y + (v.y * scale)));
		g2d.fillOval((int) (position.x + (v.x * scale) - 2.5), (int) (position.y + (v.y * scale) - 2.5), 5, 5);
	}

	/**
	 * Draws a {@link Point2d}
	 * 
	 * @param g2d
	 * @param p
	 * @param c
	 * @param radius
	 */
	protected void drawPoint(Graphics2D g2d, Point2d p, Color c, double radius) {
		g2d.setPaint(c);
		g2d.fillOval((int) (p.x - (radius / 2.0d)), (int) (p.y - (radius / 2.0d)), (int) radius, (int) radius);
	}

	protected void drawLifeBar(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(2.0f));

		if (life >= 0.5) {
			g2d.setColor(Color.green);
		} else if (life >= 0.2) {
			g2d.setColor(Color.orange);
		} else {
			g2d.setColor(Color.red);
		}

		g2d.drawLine((int) (position.x - (getRadius() / 2.0d)), (int) (position.y + getRadius()), (int) ((position.x - (getRadius() / 2.0d)) + (life * getRadius())), (int) (position.y + getRadius()));

		g2d.setStroke(new BasicStroke());
	}

	protected void drawWarning(Graphics2D g2d, int warning) {
		switch (warning) {
		case WARNING_HEALTH:
			if (healingImage != null) {
				g2d.drawImage(healingImage, (int) (position.x - (getRadius() / 2.0d)), (int) (position.y - (getRadius() / 2.0d)), (int) getRadius(), (int) getRadius(), null);
			}

			g2d.setColor(Color.red);
			g2d.drawString("Medic !", (int) position.x, (int) position.y);

			break;

		case WARNING_AMMO:
			if (ammoImage != null) {
				g2d.drawImage(ammoImage, (int) (position.x - (getRadius() / 2.0d)), (int) (position.y - (getRadius() / 2.0d)), (int) getRadius(), (int) getRadius(), null);
			}

			g2d.setColor(Color.red);
			g2d.drawString("Need ammo !", (int) position.x, (int) position.y);
			break;

		default:
			System.err.println("Unknown warning " + warning);
		}
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
	
	/**
	 * Shoot in a direction with the current carried {@link Weapon}
	 * @param env
	 * @param p
	 */
	protected void shootAt(BattleField env, Point2d p) {
		Vector2d aim = new Vector2d(p.x - position.x, p.y - position.y);

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
	
	public void setLogo(Image logo) {
		this.logo = logo;
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
