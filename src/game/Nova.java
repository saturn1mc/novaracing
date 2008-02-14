/**
 * 
 */
package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import environment.Circuit;
import environment.Environment;
import environment.Waypoint;

/**
 * @author camille
 *
 */
public class Nova extends JPanel {

	/**
	 * Generated SVUID
	 */
	private static final long serialVersionUID = 1L;
	
	private Environment environment;
	
	/**
	 * Default instance of {@link Nova}
	 */
	public Nova(){
		environment = new Environment();
		setPreferredSize(new Dimension(420, 400));
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(new GradientPaint(0, 0, Color.LIGHT_GRAY, getWidth(), getHeight(), Color.DARK_GRAY));
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		environment.draw(g2d);
	}
	
	public void loadTestRace(){
		Circuit circuit = new Circuit();
		
		Waypoint wp1 = new Waypoint(new Point2d(400, 100), 10);
		Waypoint wp2 = new Waypoint(new Point2d(200, 100), 10);
		Waypoint wp3 = new Waypoint(new Point2d(20, 200), 10);
		Waypoint wp4 = new Waypoint(new Point2d(200, 300), 10);
		Waypoint wp5 = new Waypoint(new Point2d(200, 300), 10);
		Waypoint wp6 = new Waypoint(new Point2d(200, 200), 10);
		
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
	}
	
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Nova Racing");
		
		Nova nova = new Nova();
		nova.loadTestRace();
	
		frame.add(nova);
		
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}
}
