package battlefield;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import battlefield.bots.Bot;
import battlefield.bots.Follower;
import battlefield.bots.Leader;
import battlefield.surface.Surface;
import battlefield.weapons.Bullet;
import battlefield.weapons.Pistol;
import battlefield.weapons.Weapon;

public class BattleField extends JPanel {
	/**
	 * Generated SVUID
	 */
	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int RED_TEAM_SIZE = 9;
	public static final int BLUE_TEAM_SIZE = 9;

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

	public BattleField() {
		super();

		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
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
		//addMouseMotionListener(mouse);

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
		initBots();

	}

	public void initSurface() {
		surface = new Surface(WIDTH, HEIGHT, 60);
	}

	public void initWeapons() {
		weaponsOnGround = new LinkedList<Weapon>();
		flyingBullets = new LinkedList<Bullet>();
	}

	public void initBots() {

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

		redLeader.addEnemies(blueTeam);
		blueLeader.addEnemies(redTeam);

		//WEAPON TEST
		for (Bot b : redTeam) {
			b.setCurrentWeapon(new Pistol(new Point2d(0, 0)));
		}

		for (Bot b : blueTeam) {
			b.setCurrentWeapon(new Pistol(new Point2d(0, 0)));
		}
		//
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.clearRect(0, 0, WIDTH, HEIGHT);

		surface.draw(g);

		updateWeapons();
		drawWeapons(g);

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
	}

	public void updateBots() {
		for (Bot b : redTeam) {
			b.update(this);
		}

		for (Bot b : blueTeam) {
			b.update(this);
		}
	}

	public void drawBots(Graphics g) {
		for (Bot b : redTeam) {
			b.draw((Graphics2D) g);
		}

		for (Bot b : blueTeam) {
			b.draw((Graphics2D) g);
		}
	}

	public void updateWeapons() {
		for (Bullet b : flyingBullets) {
			b.update(this);
		}

		for (Bullet b : flyingBullets) {
			if (!b.isFlying()) {

			}
		}
	}

	public void drawWeapons(Graphics g) {
		for (Weapon w : weaponsOnGround) {
			w.draw((Graphics2D) g);
		}

		for (Bullet b : flyingBullets) {
			if (b.isFlying()) {
				b.draw((Graphics2D) g);
			}
		}
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
						repaint();
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

		JFrame f = new JFrame("Battlefield (beta)");

		f.add(bf);
		f.addMouseListener(bf.getMouse());
		f.addMouseMotionListener(bf.getMouse());
		f.addKeyListener(bf.getKeyboard());

		f.pack();
		f.setLocationRelativeTo(null);

		f.setVisible(true);
		
		bf.getAnimationThread().start();
	}
}
