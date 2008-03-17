package battlefield;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;

import javax.vecmath.Point2d;

import battlefield.bots.Bot;
import battlefield.bots.Follower;
import battlefield.bots.Leader;
import battlefield.surface.Surface;
import battlefield.weapons.Bullet;
import battlefield.weapons.Pistol;
import battlefield.weapons.Weapon;

public class BattleField extends Applet implements Runnable, MouseListener, MouseMotionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int RED_TEAM_SIZE = 9;
	public static final int BLUE_TEAM_SIZE = 3;

	private Surface surface;
	
	//Teams	
	private Leader redLeader;
	private LinkedList<Bot> redTeam;
	
	private Leader blueLeader;
	private LinkedList<Bot> blueTeam;

	private LinkedList<Weapon> weaponsOnGround;
	private LinkedList<Bullet> flyingBullets;

	public void init() {
		super.init();

		resize(WIDTH, HEIGHT);
		canvasimage = createImage(WIDTH, HEIGHT);
		canvasG = canvasimage.getGraphics();
		myG = getGraphics();

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		initSurface();
		initWeapons();
		initBots();
	}

	public void initSurface() {
		surface = new Surface(WIDTH, HEIGHT, 60);
	}
	
	public void initWeapons(){
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
		for(Bot b : redTeam){
			b.setCurrentWeapon(new Pistol(new Point2d(0, 0)));
		}
		
		for(Bot b : blueTeam){
			b.setCurrentWeapon(new Pistol(new Point2d(0, 0)));
		}
		//
	}

	public boolean handleEvent(Event event) {
		boolean returnValue = false;
		return returnValue;
	}

	public static void main(String args[]) {
		Frame f = new Frame();
		BattleField app = new BattleField();
		app.init();
		app.start();
		f.add("Center", app);

	}

	public void start() {
		if (update == null) {
			update = new Thread(this);
			update.start();
		}
	}

	public void stop() {
		update = null;
	}

	// The main loop of the applet... We sleep a lot...
	public void run() {
		do {
			repaint();
			try {
				Thread.sleep(5);
			} catch (InterruptedException _ex) {
				_ex.printStackTrace();
			}
		} while (true);
	}

	// Use very simple double buffering technique...
	private void showbuffer() {
		myG.drawImage(canvasimage, 0, 0, this);
	}

	// repaint all components
	public void paint(Graphics g) {
		canvasG.setColor(bgColor);
		canvasG.fillRect(0, 0, WIDTH, HEIGHT);
		surface.draw(canvasG);

		updateWeapons();
		drawWeapons(canvasG);
		
		updateBots();
		drawBots(canvasG);

		canvasG.setColor(Color.black);
		canvasG.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

		if ((pointA.x > -1) && (pointB.x > -1)) {
			if (surface.canSee(pointA, pointB))
				canvasG.setColor(Color.green);
			else
				canvasG.setColor(Color.red);
			canvasG.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
		}
		
		drawGUI();
		showbuffer();
	}

	// Very simple GUI.. Just print the infos string on the bottom of the screen
	private void drawGUI() {
		canvasG.setColor(Color.red);
		canvasG.drawRect(20, HEIGHT - 23, WIDTH - 41, 20);
		canvasG.drawChars(infos.toCharArray(), 0, Math.min(50, infos.length()), 22, HEIGHT - 7);
	}

	// Update bots positions
	public void updateBots() {
		for(Bot b : redTeam){
			b.update(this);
		}
		
		for(Bot b : blueTeam){
			b.update(this);
		}
	}

	// Draw bots
	public void drawBots(Graphics g) {
		for(Bot b : redTeam){
			b.draw((Graphics2D)g);
		}
		
		for(Bot b : blueTeam){
			b.draw((Graphics2D)g);
		}
	}
	
	public void updateWeapons() {
		for (Bullet b : flyingBullets) {
			b.update(this);
		}
		
		for (Bullet b : flyingBullets) {
			if(!b.isFlying()){
				
			}
		}
	}

	public void drawWeapons(Graphics g) {
		for (Weapon w : weaponsOnGround) {
			w.draw((Graphics2D) g);
		}

		for (Bullet b : flyingBullets) {
			if(b.isFlying()){
				b.draw((Graphics2D) g);
			}
		}
	}
	
	public void fireBullet(Bullet bullet){
		flyingBullets.add(bullet);
	}

	// Reset positions and data of all bots
	public void resetBots() {
	}

	// Simply repaint the battle field... Called every frame...
	public void update(Graphics g) {
		paint(g);
	}

	// Very simple constructor
	public BattleField() {
	}

	public Surface getSurface() {
		return surface;
	}

	private Thread update;
	Point2d viewCenter;
	int framesSinceTouch;
	Image canvasimage;
	Graphics canvasG;
	Graphics myG;
	String infos = "";
	static final Color bgColor = new Color(0.9F, 0.9F, 0.6F);
	private Point2d pointA = new Point2d(-1, -1);
	private Point2d pointB = new Point2d(-1, -1);

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		pointA.x = e.getX();
		pointA.y = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		pointB.x = e.getX();
		pointB.y = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
	}

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
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
