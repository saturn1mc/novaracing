package battlefield;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.vecmath.Point2d;

import battlefield.aStar.AStar;
import battlefield.bonuses.AmmoPoint;
import battlefield.bonuses.LifePoint;
import battlefield.bots.Bot;
import battlefield.bots.Follower;
import battlefield.bots.HumanLeader;
import battlefield.bots.Leader;
import battlefield.surface.Surface;
import battlefield.surface.Waypoint;
import battlefield.weapons.Bullet;
import battlefield.weapons.Pistol;
import battlefield.weapons.SubmachineGun;
import battlefield.weapons.Weapon;

public class BattleField extends JFrame {

	//Constants
	/**
	 * Generated SVUID
	 */
	private static final long serialVersionUID = 1L;
	
	// Human leader selection
	private static final boolean humanRedLeader = true;
	private static final boolean humanBlueLeader = false;
	
	private static Image blueTeamLogo;
	private static Image redTeamLogo;
	
	/**
	 * Frame width
	 */
	public static final int WIDTH = 800;

	/**
	 * Frame height
	 */
	public static final int HEIGHT = 600;

	/**
	 * Number of unit in the red team (excluding the leader)
	 */
	public static final int RED_TEAM_SIZE = 9;

	/**
	 * Number of unit in the blue team (excluding the leader)
	 */
	public static final int BLUE_TEAM_SIZE = 9;

	/**
	 * Number of bonus point on the map
	 */
	public static final int BONUS_POINTS_NB = 20;

	// Interaction
	/**
	 * True if the animation must be played
	 */
	private boolean play;

	// Environment
	/**
	 * The battlefield {@link Surface}
	 */
	private Surface surface;

	// Teams
	/**
	 * Red team {@link Leader} bot
	 */
	private Leader redLeader;

	/**
	 * Red team {@link Follower} list
	 */
	private LinkedList<Bot> redTeam;

	/**
	 * Blue {@link Leader} bot
	 */
	private Leader blueLeader;

	/**
	 * Blue team {@link Follower} list
	 */
	private LinkedList<Bot> blueTeam;

	// Weapons
	/**
	 * Weapons available on the battlefield
	 */
	private LinkedList<Weapon> weaponsOnGround;

	/**
	 * List of {@link Bullet} that have been shot and still flying
	 */
	private LinkedList<Bullet> flyingBullets;

	// Bonus point
	/**
	 * {@link LifePoint} list
	 */
	private LinkedList<LifePoint> lifePoints;

	/**
	 * {@link AmmoPoint} list
	 */
	private LinkedList<AmmoPoint> ammoPoints;

	/**
	 * Default (and currently the only) constructor for the {@link BattleField}
	 */
	public BattleField() {
		super("Battlefield - Boutet, Maurice 2008");

		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);

		play = false;

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					setPlay(!playing());
				}
			}
		});

		if (redTeamLogo == null) {
			try {
				redTeamLogo = ImageIO.read(getClass().getResource("/images/redTeam.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (blueTeamLogo == null) {
			try {
				blueTeamLogo = ImageIO.read(getClass().getResource("/images/blueTeam.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		initSurface();
		initWeapons();
		initBonuses();
		initBots();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);

		if (b) {
			this.createBufferStrategy(2); // Buffering
		}
	}

	/**
	 * Initializes the {@link Surface}
	 */
	private void initSurface() {
		surface = new Surface(WIDTH, HEIGHT, 70);
	}

	/**
	 * Initializes the {@link Weapon} and {@link Bullet} lists
	 */
	private void initWeapons() {
		weaponsOnGround = new LinkedList<Weapon>();
		flyingBullets = new LinkedList<Bullet>();
	}

	/**
	 * Initializes bonus points ({@link LifePoint} and {@link AmmoPoint})
	 */
	private void initBonuses() {

		LinkedList<Point2d> used = new LinkedList<Point2d>();

		// Life points
		lifePoints = new LinkedList<LifePoint>();

		int added = 0;

		while (added < BONUS_POINTS_NB) {

			Point2d p = surface.getGraph().getRandomPoint();

			if (!used.contains(p) && surface.goodPoint(p)) {
				lifePoints.add(new LifePoint(p, 100));
				used.add(p);
			}

			added++;
		}

		// Ammo points
		ammoPoints = new LinkedList<AmmoPoint>();

		added = 0;

		while (added < BONUS_POINTS_NB) {

			Point2d p = surface.getGraph().getRandomPoint();

			if (!used.contains(p) && surface.goodPoint(p)) {
				ammoPoints.add(new AmmoPoint(p, 1000));
				used.add(p);
			}

			added++;
		}

	}

	/**
	 * Initializes the teams
	 */
	private void initBots() {

		redTeam = new LinkedList<Bot>();
		blueTeam = new LinkedList<Bot>();

		// Red team
		if (humanRedLeader) {
			redLeader = new HumanLeader(this, "Nova", new Point2d(100, 100), Color.red, Leader.FORMATION_SQUARE);
			this.addMouseListener(((HumanLeader) redLeader).getMouse());
			this.addMouseMotionListener(((HumanLeader) redLeader).getMouse());
			this.addKeyListener(((HumanLeader) redLeader).getKeyboard());
			//
		} else {
			redLeader = new Leader("RedLeader", new Point2d(100, 100), Color.red, Leader.FORMATION_SQUARE);
		}

		redLeader.setLogo(redTeamLogo);
		redLeader.setCurrentWeapon(new SubmachineGun(null));
		redTeam.add(redLeader);

		for (int i = 0; i < RED_TEAM_SIZE; i++) {
			Follower f = new Follower("Red_" + i, new Point2d(101, 101), Color.red);
			f.setCurrentWeapon(new Pistol(null));
			f.setLeader(redLeader);
			f.setLogo(redTeamLogo);
			redLeader.registerFollower(f);
			redTeam.add(f);
		}

		// Blue team
		if (humanBlueLeader) {
			blueLeader = new HumanLeader(this, "Nova", new Point2d(100, 100), Color.blue, Leader.FORMATION_SQUARE);
			redTeam.add(blueLeader);
			this.addMouseListener(((HumanLeader) blueLeader).getMouse());
			this.addMouseMotionListener(((HumanLeader) blueLeader).getMouse());
			this.addKeyListener(((HumanLeader) blueLeader).getKeyboard());
			//
		} else {
			blueLeader = new Leader("BlueLeader", new Point2d(BattleField.WIDTH - 100, (BattleField.HEIGHT - 100)), Color.blue, Leader.FORMATION_SQUARE);
		}

		blueLeader.setLogo(blueTeamLogo);
		blueLeader.setCurrentWeapon(new SubmachineGun(null));
		blueTeam.add(blueLeader);

		for (int i = 0; i < BLUE_TEAM_SIZE; i++) {
			Follower f = new Follower("Blue_" + i, new Point2d((BattleField.WIDTH - 101), (BattleField.HEIGHT - 101)), Color.blue);
			f.setCurrentWeapon(new Pistol(null));
			f.setLeader(blueLeader);
			f.setLogo(blueTeamLogo);
			blueLeader.registerFollower(f);
			blueTeam.add(f);
		}

		// Enemies
		redLeader.addEnemies(blueTeam);
		blueLeader.addEnemies(redTeam);

		for (Bot b : redTeam) {
			b.addEnemies(blueTeam);
		}

		for (Bot b : blueTeam) {
			b.addEnemies(redTeam);
		}
	}

	/**
	 * Updates the {@link Weapon} and {@link Bullet} lists (removes picked
	 * weapon and non flying bullets if any)
	 */
	private void updateWeapons() {

		LinkedList<Bullet> toDelete = new LinkedList<Bullet>();

		for (Bullet b : flyingBullets) {
			b.update(this);
			if (!b.isFlying()) {
				toDelete.add(b);
			}
		}

		for (Bullet b : toDelete) {
			flyingBullets.remove(b);
		}
	}

	/**
	 * Updates the bonus points (removes empty {@link AmmoPoint} and
	 * {@link LifePoint} if any)
	 */
	private void updateBonusPoints() {

		LinkedList<Waypoint> emptyPoints = new LinkedList<Waypoint>();

		for (LifePoint lp : lifePoints) {
			if (lp.isEmpty()) {
				emptyPoints.add(lp);
			}
		}

		for (AmmoPoint ap : ammoPoints) {
			if (ap.isEmpty()) {
				emptyPoints.add(ap);
			}
		}

		for (Waypoint wp : emptyPoints) {
			lifePoints.remove(wp);
			ammoPoints.remove(wp);
		}
	}

	/**
	 * Updates very {@link Bot}
	 */
	private void updateBots() {
		// See who's alive
		LinkedList<Bot> deads = new LinkedList<Bot>();

		for (Bot b : redTeam) {
			if (!b.isAlive()) {
				deads.add(b);
			}
		}

		for (Bot b : blueTeam) {
			if (!b.isAlive()) {
				deads.add(b);
			}
		}

		for (Bot dead : deads) {
			blueTeam.remove(dead);
			redTeam.remove(dead);
		}

		// Draw survivors
		for (Bot b : redTeam) {
			b.update(this);
		}

		for (Bot b : blueTeam) {
			b.update(this);
		}
	}

	/**
	 * Calls draw method for every {@link Bullet} and {@link Weapon}
	 * 
	 * @param g
	 */
	private void drawWeapons(Graphics g) {
		for (Weapon w : weaponsOnGround) {
			w.draw((Graphics2D) g);
		}

		for (Bullet b : flyingBullets) {
			if (b.isFlying()) {
				b.hitTest(this);
				b.draw((Graphics2D) g);
			}
		}
	}

	/**
	 * Calls draw method for every {@link LifePoint} and {@link AmmoPoint}
	 * 
	 * @param g
	 */
	private void drawBonusPoint(Graphics g) {
		for (LifePoint lp : lifePoints) {
			lp.draw((Graphics2D) g);
		}

		for (AmmoPoint ap : ammoPoints) {
			ap.draw((Graphics2D) g);
		}
	}

	/**
	 * Calls draw method for every {@link Bot}
	 * 
	 * @param g
	 */
	private void drawBots(Graphics g) {
		for (Bot b : redTeam) {
			b.draw((Graphics2D) g);
		}

		for (Bot b : blueTeam) {
			b.draw((Graphics2D) g);
		}
	}

	/**
	 * Battlefield drawing method (uses {@link BufferStrategy} to optimize
	 * rendering speed)
	 */
	private void drawBattlefield() {

		BufferStrategy bf = this.getBufferStrategy();

		if (bf != null) {
			Graphics g = bf.getDrawGraphics();

			g.clearRect(0, 0, WIDTH, HEIGHT);

			surface.draw(g);
			drawWeapons(g);
			drawBonusPoint(g);
			drawBots(g);

			bf.show();

			// Tell the System to do the Drawing now, otherwise it can take a
			// few extra ms until drawing is done
			Toolkit.getDefaultToolkit().sync();

			g.dispose();
		}
	}

	/**
	 * Draws an info over the {@link BattleField}
	 * 
	 * @param info
	 *            the string to show
	 * @param obscureBattlefield
	 *            true if the {@link BattleField} must be obscured, or else
	 *            false
	 */
	private void drawInfo(String info, boolean obscureBattlefield) {
		BufferStrategy bf = this.getBufferStrategy();

		if (bf != null) {
			Graphics g = bf.getDrawGraphics();

			surface.draw(g);
			drawWeapons(g);
			drawBonusPoint(g);
			drawBots(g);

			if (obscureBattlefield) {
				g.setColor(new Color(0, 0, 0, 0.8f));
				g.fillRect(0, 0, WIDTH, HEIGHT);

				g.setColor(Color.white);
			} else {
				g.setColor(Color.black);
			}

			int infoStrWidth = g.getFontMetrics().stringWidth(info);
			g.drawString(info, (WIDTH / 2) - (infoStrWidth / 2), HEIGHT / 2);

			bf.show();

			// Tell the System to do the Drawing now, otherwise it can take a
			// few extra ms until drawing is done
			Toolkit.getDefaultToolkit().sync();

			g.dispose();
		}
	}

	/**
	 * Getter for the nearest {@link LifePoint} from a given {@link Bot}
	 * 
	 * @param b
	 *            the given {@link Bot}
	 * @return the nearest {@link LifePoint} if any, or else <code>null</code>
	 */
	public LifePoint nearestLifePoint(Bot b) {
		double distance_min = Double.MAX_VALUE;
		LifePoint result = null;
		for (LifePoint lp : lifePoints) {
			double distance = AStar.distance(lp.getPosition(), b.getPosition());
			if (lp.getLife() > 0 && distance_min > distance) {
				distance_min = distance;
				result = lp;
			}
		}
		return result;
	}

	/**
	 * Getter for the nearest {@link AmmoPoint} from a given {@link Bot}
	 * 
	 * @param b
	 *            the given {@link Bot}
	 * @return the nearest {@link AmmoPoint} if any, or else <code>null</code>
	 */
	public AmmoPoint nearestAmmoPoint(Bot b) {
		double distance_min = Double.MAX_VALUE;
		AmmoPoint result = null;
		for (AmmoPoint ap : ammoPoints) {
			double distance = AStar.distance(ap.getPosition(), b.getPosition());
			if (ap.getAmmo() > 0 && distance_min > distance) {
				distance_min = distance;
				result = ap;
			}

		}
		return result;
	}

	public LinkedList<Bot> getRedTeam() {
		return redTeam;
	}

	public LinkedList<Bot> getBlueTeam() {
		return blueTeam;
	}

	public void fireBullet(Bullet bullet) {
		flyingBullets.add(bullet);
	}

	public Surface getSurface() {
		return surface;
	}

	public synchronized boolean playing() {
		return play;
	}

	public synchronized void setPlay(boolean play) {
		this.play = play;
	}

	/**
	 * The {@link BattleField} animation thread
	 * 
	 * @return
	 */
	public Thread getAnimationThread() {
		Thread aT = new Thread() {
			@Override
			public void run() {
				do {
					if (isVisible()) {
						if (playing()) {

							updateWeapons();
							updateBonusPoints();
							updateBots();

							drawBattlefield();

						} else {
							drawInfo("Press 'space' to play", true);
						}
					}

					try {
						sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} while (true);
			}
		};

		return aT;
	}

	public static void main(String args[]) {
		BattleField bf = new BattleField();
		bf.getAnimationThread().start();
		bf.setVisible(true);
	}
}
