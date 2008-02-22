/**
 * 
 */
package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import elements.HumanVehicle;
import elements.Obstacle;
import elements.Vehicle;
import elements.Waypoint;
import environment.Circuit;
import environment.Environment;

/**
 * @author camille
 * 
 */
public class Nova extends JPanel {

	private boolean run;

	/**
	 * Generated SVUID
	 */
	private static final long serialVersionUID = 1L;

	private Environment environment;

	/**
	 * Default instance of {@link Nova}
	 */
	public Nova() {
		run = true;
		environment = new Environment();
		setPreferredSize(new Dimension(600, 400));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		Graphics2D g2d = (Graphics2D) g;
		
		/* Filling the background */
		g2d.setPaint(new GradientPaint(0, 0, new Color(0.0f, 0.5f, 0.0f), getWidth(), getHeight(), new Color(0.0f, 0.7f, 0.0f)));
		g2d.fillRect(0, 0, getWidth(), getHeight());

		environment.draw(g2d);
	}

	/**
	 * Updates the associated environment
	 */
	public void update() {
		if(isRunning()){
			environment.update();
			repaint();
		}
	}

	/**
	 * Run indicator
	 * @return <code>true</code> if {@link Nova} is running, or else false
	 */
	public synchronized boolean isRunning() {
		return run;
	}

	/**
	 * Setter for the running state
	 * @param running
	 */
	public synchronized void setRunning(boolean running) {
		run = running;
	}

	/**
	 * Loads the first default circuit with some vehicles
	 */
	public void loadTestRace() {
		
		Circuit circuit = new Circuit();

		/*
		 * Waypoints
		 */
		Waypoint wp1 = new Waypoint(new Point2d(400, 100));
		Waypoint wp2 = new Waypoint(new Point2d(200, 100));
		Waypoint wp3 = new Waypoint(new Point2d(20, 200));
		Waypoint wp4 = new Waypoint(new Point2d(200, 300));
		Waypoint wp5 = new Waypoint(new Point2d(400, 300));
		Waypoint wp6 = new Waypoint(new Point2d(580, 200));

		wp1.setNext(wp2);
		wp1.setPrevious(wp6);
		wp2.setNext(wp3);
		wp2.setPrevious(wp1);
		wp3.setNext(wp4);
		wp3.setPrevious(wp2);
		wp4.setNext(wp5);
		wp4.setPrevious(wp3);
		wp5.setNext(wp6);
		wp5.setPrevious(wp4);
		wp6.setNext(wp1);
		wp6.setPrevious(wp5);

		circuit.addWaypoint(wp1);
		circuit.addWaypoint(wp2);
		circuit.addWaypoint(wp3);
		circuit.addWaypoint(wp4);
		circuit.addWaypoint(wp5);
		circuit.addWaypoint(wp6);

		environment.setCircuit(circuit);

		/*
		 * Vehicles
		 */
		Vehicle v1 = new Vehicle("v1", new Point2d(wp1.getPosition()), wp1.getNext());
		environment.addElement(v1);
		
		Vehicle v2 = new Vehicle("v2", new Point2d(wp2.getPosition()), wp2.getNext());
		environment.addElement(v2);
		
		/*
		 * Human
		 */
		HumanVehicle human = new HumanVehicle("saturn1", new Point2d(wp6.getPosition()), wp6.getNext());
		environment.addElement(human);
		addKeyListener(human.getKeyboard());
	}
	
	/**
	 * Loads the second default circuit with some vehicles
	 */
	public void loadTestRace2() {
		
		Circuit circuit = new Circuit();

		/*
		 * Waypoints
		 */
		Waypoint wp1 = new Waypoint(new Point2d(200, 20));
		Waypoint wp2 = new Waypoint(new Point2d(20, 150));
		Waypoint wp3 = new Waypoint(new Point2d(200, 200));
		Waypoint wp4 = new Waypoint(new Point2d(300, 300));
		Waypoint wp5 = new Waypoint(new Point2d(580, 380));
		Waypoint wp6 = new Waypoint(new Point2d(400, 40));

		wp1.setNext(wp2);
		wp1.setPrevious(wp6);
		wp2.setNext(wp3);
		wp2.setPrevious(wp1);
		wp3.setNext(wp4);
		wp3.setPrevious(wp2);
		wp4.setNext(wp5);
		wp4.setPrevious(wp3);
		wp5.setNext(wp6);
		wp5.setPrevious(wp4);
		wp6.setNext(wp1);
		wp6.setPrevious(wp5);

		circuit.addWaypoint(wp1);
		circuit.addWaypoint(wp2);
		circuit.addWaypoint(wp3);
		circuit.addWaypoint(wp4);
		circuit.addWaypoint(wp5);
		circuit.addWaypoint(wp6);

		environment.setCircuit(circuit);

		/*
		 * Vehicles
		 */
		Vehicle v1 = new Vehicle("v1", new Point2d(wp1.getPosition()), wp1.getNext());
		environment.addElement(v1);
		
		Vehicle v2 = new Vehicle("v2", new Point2d(wp2.getPosition()), wp2.getNext());
		environment.addElement(v2);
		
		Vehicle v3 = new Vehicle("v3", new Point2d(wp3.getPosition()), wp3.getNext());
		environment.addElement(v3);
		
		Vehicle v4 = new Vehicle("v4", new Point2d(wp4.getPosition()), wp4.getNext());
		environment.addElement(v4);
		
		/*
		 * Obstacles
		 */
		Obstacle o1 = new Obstacle("wall", new Point2d(wp6.getPosition().x - 10, wp6.getPosition().y + 50), 30);
		environment.addElement(o1);
		
		Obstacle o2 = new Obstacle("wall2", new Point2d(wp1.getPosition().x - 50, wp1.getPosition().y), 30);
		environment.addElement(o2);
		
		Obstacle o3 = new Obstacle("wall3", new Point2d(wp4.getPosition().x, wp4.getPosition().y - 50), 30);
		environment.addElement(o3);
		
		/*
		 * Human
		 */
		HumanVehicle human = new HumanVehicle("saturn1", new Point2d(wp6.getPosition()), wp6.getNext());
		environment.addElement(human);
		addKeyListener(human.getKeyboard());
	}

	public Thread getAnimationThread() {
		return new Thread() {
			public void run() {
				do {
					Nova.this.update();
					try {
						Thread.sleep(5);
					} catch (InterruptedException _ex) {
						//nothing
					}
				} while (isRunning());
			}
		};
	}

	public static void main(String[] args) {

		JFrame frame = new JFrame("Nova Racing");
		
		Nova nova = new Nova();
		nova.loadTestRace2();

		frame.add(nova);
		
		for(KeyListener kl : nova.getKeyListeners()){
			frame.addKeyListener(kl);
		}

		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		frame.setVisible(true);

		nova.setRunning(true);
		nova.getAnimationThread().start();
	}
}
