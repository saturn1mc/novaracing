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
import java.util.Random;

import javax.vecmath.Point2d;

import battlefield.aStar.Path;
import battlefield.bots.Follower;
import battlefield.bots.Leader;
import battlefield.surface.Surface;

public class BattleField extends Applet implements Runnable, MouseListener, MouseMotionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Surface surface;
	private Leader leader;
	private LinkedList<Follower> followers;

	public static final int NB_FOLLOWERS = 9;
	
	public static final float MAXX = 10000F; // Size of the battlefield, in
	// float (not pixels)
	public static final float MAXY = 7500F;

	public static final int WXSIZE = 800; // size in pixels (in x, the y is

	// automatically deduced)

	public void init() {
		super.init();

		wxsize = WXSIZE; // size in pixels
		wysize = (int) (MAXY / scale); // The y axe is automatically computed

		resize(wxsize, wysize);
		canvasimage = createImage(wxsize, wysize);
		canvasG = canvasimage.getGraphics();
		myG = getGraphics();

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		initSurface();
		initBots();
	}

	public void initSurface() {
		surface = new Surface(wxsize, wysize, scale, 50);
	}

	public void initBots() {
		leader = new Leader("Nova", new Point2d(0, 0), Leader.FORMATION_SQUARE);
		followers = new LinkedList<Follower>();

		Random random = new Random();

		for (int i = 0; i < NB_FOLLOWERS; i++) {
			Follower f = new Follower("Soldier_" + i, new Point2d(random.nextInt(200), random.nextInt(200)));
			followers.add(f);

			f.setLeader(leader);
			leader.registerFollower(f);
		}
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
		canvasG.fillRect(0, 0, wxsize, wysize);
		surface.draw(canvasG);

		updateBots();
		drawBots(canvasG);

		canvasG.setColor(Color.black);
		canvasG.drawRect(0, 0, wxsize - 1, wysize - 1);

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
		canvasG.drawRect(20, wysize - 23, wxsize - 41, 20);
		canvasG.drawChars(infos.toCharArray(), 0, Math.min(50, infos.length()), 22, wysize - 7);
	}

	// Update bots positions and
	public void updateBots() {
		leader.update(this);

		for (Follower f : followers) {
			f.update(this);
		}
	}

	public void drawBots(Graphics g) {
		leader.draw((Graphics2D) g);

		for (Follower f : followers) {
			f.draw((Graphics2D) g);
		}
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
		scale = MAXX / WXSIZE;
	}

	public Surface getSurface() {
		return surface;
	}

	private Thread update;
	// MyBot bot;
	float scale;
	Point2d viewCenter;
	int framesSinceTouch;
	Image canvasimage;
	Graphics canvasG;
	Graphics myG;
	int wxsize;
	int wysize;
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
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {

		pointA.x = leader.getPosition().x;
		pointA.y = leader.getPosition().y;

		pointB.x = e.getX();
		pointB.y = e.getY();

		Path path = surface.solve(pointA, pointB);

		if (path != null) {
			leader.setTarget(path.getPoints().getFirst());
		}

		repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_S) {
			// Square formation
			leader.setFormationOrder(Leader.FORMATION_SQUARE);
		}

		if (e.getKeyCode() == KeyEvent.VK_L) {
			// Line formation
			leader.setFormationOrder(Leader.FORMATION_LINE);
		}
		
		if (e.getKeyCode() == KeyEvent.VK_W) {
			// Wing formation
			leader.setFormationOrder(Leader.FORMATION_WING);
		}
		
		if (e.getKeyCode() == KeyEvent.VK_B) {
			// Shield formation
			leader.setFormationOrder(Leader.FORMATION_SHIELD);
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
