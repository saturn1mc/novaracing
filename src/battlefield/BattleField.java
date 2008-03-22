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
import battlefield.bonus.AmmoPoint;
import battlefield.bonus.LifePoint;
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

	public static final int BONUS_POINTS_NB = 10;

	private boolean playing;

	private Point2d pointA;
	private Point2d pointB;

	private MouseAdapter mouse;
	private KeyAdapter keyboard;

	//Environment
	private Surface surface;

	//Teams	
	private Leader redLeader;
	private LinkedList<Bot> redTeam;

	private Leader blueLeader;
	private LinkedList<Bot> blueTeam;

	//Weapons
	private LinkedList<Weapon> weaponsOnGround;
	private LinkedList<Bullet> flyingBullets;

	//Bonus point
	private LinkedList<Waypoint> bonusPoints;

	public BattleField() {
		super("Battlefield - Boutet, Maurice 2008");

		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.pack();
		this.setResizable(false);

		playing = false;

		pointA = new Point2d();
		pointB = new Point2d();

		this.mouse = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pointA.x = e.getX();
				pointA.y = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				pointB.x = e.getX();
				pointB.y = e.getY();
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
			this.createBufferStrategy(2); //Buffering
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
		bonusPoints = new LinkedList<Waypoint>();

		for (int i = 0; i < BONUS_POINTS_NB; i++) {
			if (i % 2 == 0) {
				Point2d p = surface.getGraph().getRandomPoint();
				if (surface.goodPoint(p)) {
					bonusPoints.add(new LifePoint(p, 10));
				}
			} else {
				Point2d p = surface.getGraph().getRandomPoint();
				if (surface.goodPoint(p)) {
					bonusPoints.add(new AmmoPoint(p, 100));
				}
			}
		}
	}

	private void initBots() {

		redTeam = new LinkedList<Bot>();
		blueTeam = new LinkedList<Bot>();

		//Red team
		redLeader = new Leader("RedLeader", new Point2d(100, 100), Color.red, Leader.FORMATION_SQUARE);
		redTeam.add(redLeader);

		for (int i = 0; i < RED_TEAM_SIZE; i++) {
			Follower f = new Follower("Red_" + i, new Point2d(101, 101), Color.red);
			f.setLeader(redLeader);
			redLeader.registerFollower(f);
			redTeam.add(f);
		}

		//Blue team
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
		//TODO
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
		for (Waypoint bp : bonusPoints) {
			bp.draw((Graphics2D) g);
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
		Graphics g = bf.getDrawGraphics();

		g.clearRect(0, 0, WIDTH, HEIGHT);

		surface.draw(g);

		updateWeapons();
		drawWeapons(g);

		updateBonusPoints();
		drawBonusPoint(g);

		updateBots();
		drawBots(g);

		if ((pointA.x > -1) && (pointB.x > -1)) {
			if (surface.canSee(pointA, pointB)) {
				g.setColor(Color.green);
			} else {
				g.setColor(Color.red);
			}
			g.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
		}

		bf.show();

		//Tell the System to do the Drawing now, otherwise it can take a few extra ms until drawing is done
		Toolkit.getDefaultToolkit().sync();
	}

	public LifePoint getPainKiller(Bot b) {
		double distance_min = Double.MAX_VALUE;
		LifePoint result = null;
		for (Waypoint w : bonusPoints) {
			if (w instanceof LifePoint) {
				LifePoint lp = (LifePoint) w;
				double distance = AStar.distance(lp.getPosition(), b.getPosition());
				if (lp.getLife() > 0 && distance_min > distance) {
					distance_min = distance;
					result = lp;
				}
			}
		}
		return result;
	}

	public AmmoPoint getReload(Bot b) {
		double distance_min = Double.MAX_VALUE;
		AmmoPoint result = null;
		for (Waypoint w : bonusPoints) {
			if (w instanceof AmmoPoint) {
				AmmoPoint ap = (AmmoPoint) w;
				double distance = AStar.distance(ap.getPosition(), b.getPosition());
				if (ap.getAmmo() > 0 && distance_min > distance) {
					distance_min = distance;
					result = ap;
				}
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
					if (playing()) {
						updateWeapons();
						updateBonusPoints();
						updateBots();

						drawBattlefield();
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
