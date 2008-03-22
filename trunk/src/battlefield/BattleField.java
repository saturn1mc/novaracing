package battlefield;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.vecmath.Point2d;

import battlefield.aStar.AStar;
import battlefield.bonuses.AmmoPoint;
import battlefield.bonuses.LifePoint;
import battlefield.bots.Bot;
import battlefield.bots.Follower;
import battlefield.bots.Leader;
import battlefield.surface.Surface;
import battlefield.surface.Waypoint;
import battlefield.weapons.Bullet;
import battlefield.weapons.Pistol;
import battlefield.weapons.Weapon;

public class BattleField extends JFrame {

	/**
	 * Generated SVUID
	 */
	private static final long serialVersionUID = 1L;

	// Constants
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;

	public static final int RED_TEAM_SIZE = 9;
	public static final int BLUE_TEAM_SIZE = 9;

	public static final int BONUS_POINTS_NB = 20;

	private boolean playing;

	private MouseAdapter mouse;
	private KeyAdapter keyboard;

	// Environment
	private Surface surface;

	// Teams
	private Leader redLeader;
	private LinkedList<Bot> redTeam;

	private Leader blueLeader;
	private LinkedList<Bot> blueTeam;

	// Weapons
	private LinkedList<Weapon> weaponsOnGround;
	private LinkedList<Bullet> flyingBullets;

	// Bonus point
	private LinkedList<LifePoint> lifePoints;
	private LinkedList<AmmoPoint> ammoPoints;

	public BattleField() {
		super("Battlefield - Boutet, Maurice 2008");

		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);

		playing = false;

		this.mouse = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO
			}
		};

		addMouseListener(mouse);
		addMouseMotionListener(mouse);

		this.keyboard = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_S) {
					// Square formation
					redLeader.setFormationOrder(Leader.FORMATION_SQUARE);
				}

				if (e.getKeyCode() == KeyEvent.VK_L) {
					// Line formation
					redLeader.setFormationOrder(Leader.FORMATION_LINE);
				}

				if (e.getKeyCode() == KeyEvent.VK_W) {
					// Wing formation
					redLeader.setFormationOrder(Leader.FORMATION_WING);
				}

				if (e.getKeyCode() == KeyEvent.VK_C) {
					// Shield formation
					redLeader.setFormationOrder(Leader.FORMATION_SHIELD);
				}

				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					// Play / Pause
					setPlaying(!playing());
				}
			}
		};

		addKeyListener(keyboard);

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

	private void initSurface() {
		surface = new Surface(WIDTH, HEIGHT, 60);
	}

	private void initWeapons() {
		weaponsOnGround = new LinkedList<Weapon>();
		flyingBullets = new LinkedList<Bullet>();
	}

	private void initBonuses() {

		// Life points
		lifePoints = new LinkedList<LifePoint>();

		int added = 0;

		while (added < (BONUS_POINTS_NB / 2)) {

			Point2d p = surface.getGraph().getRandomPoint();

			if (surface.goodPoint(p)) {
				lifePoints.add(new LifePoint(p, 100));
			}

			added++;
		}

		// Ammo points
		ammoPoints = new LinkedList<AmmoPoint>();

		added = 0;

		while (added < (BONUS_POINTS_NB / 2)) {

			Point2d p = surface.getGraph().getRandomPoint();

			if (surface.goodPoint(p)) {
				ammoPoints.add(new AmmoPoint(p, 1000));
			}

			added++;
		}

	}

	private void initBots() {

		redTeam = new LinkedList<Bot>();
		blueTeam = new LinkedList<Bot>();

		// Red team
		redLeader = new Leader("RedLeader", new Point2d(100, 100), Color.red, Leader.FORMATION_SQUARE);
		redTeam.add(redLeader);

		for (int i = 0; i < RED_TEAM_SIZE; i++) {
			Follower f = new Follower("Red_" + i, new Point2d(101, 101), Color.red);
			f.setLeader(redLeader);
			redLeader.registerFollower(f);
			redTeam.add(f);
		}

		// Blue team
		blueLeader = new Leader("BlueLeader", new Point2d(BattleField.WIDTH - 100, (BattleField.HEIGHT - 100)), Color.blue, Leader.FORMATION_SQUARE);
		blueTeam.add(blueLeader);

		for (int i = 0; i < BLUE_TEAM_SIZE; i++) {
			Follower f = new Follower("Blue_" + i, new Point2d((BattleField.WIDTH - 101), (BattleField.HEIGHT - 101)), Color.blue);

			f.setLeader(blueLeader);
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

		// WEAPON TEST
		for (Bot b : redTeam) {
			b.setCurrentWeapon(new Pistol(new Point2d(0, 0)));
		}

		for (Bot b : blueTeam) {
			b.setCurrentWeapon(new Pistol(new Point2d(0, 0)));
		}
		//
	}

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

	private void drawBonusPoint(Graphics g) {
		for (LifePoint lp : lifePoints) {
			lp.draw((Graphics2D) g);
		}

		for (AmmoPoint ap : ammoPoints) {
			ap.draw((Graphics2D) g);
		}
	}

	private void drawBots(Graphics g) {
		for (Bot b : redTeam) {
			b.draw((Graphics2D) g);
		}

		for (Bot b : blueTeam) {
			b.draw((Graphics2D) g);
		}
	}

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

	private void drawInfo(String info) {
		BufferStrategy bf = this.getBufferStrategy();

		if (bf != null) {
			Graphics g = bf.getDrawGraphics();

			surface.draw(g);
			drawWeapons(g);
			drawBonusPoint(g);
			drawBots(g);

			g.setColor(new Color(0, 0, 0, 0.8f));
			g.fillRect(0, 0, WIDTH, HEIGHT);

			g.setColor(Color.white);
			int infoStrWidth = g.getFontMetrics().stringWidth(info);
			g.drawString(info, (WIDTH / 2) - (infoStrWidth / 2), HEIGHT / 2);

			bf.show();

			// Tell the System to do the Drawing now, otherwise it can take a
			// few extra ms until drawing is done
			Toolkit.getDefaultToolkit().sync();

			g.dispose();
		}
	}

	public LifePoint getPainKiller(Bot b) {
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

	public AmmoPoint getReload(Bot b) {
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

	public MouseAdapter getMouse() {
		return mouse;
	}

	public KeyAdapter getKeyboard() {
		return keyboard;
	}

	public synchronized boolean playing() {
		return playing;
	}

	public synchronized void setPlaying(boolean playing) {
		this.playing = playing;
	}

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
							drawInfo("Press 'space' to play");
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
