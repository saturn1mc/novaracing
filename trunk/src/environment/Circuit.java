/**
 * 
 */
package environment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.LinkedList;

import elements.Vehicle;
import elements.Waypoint;

/**
 * @author camille
 *
 */
public class Circuit {

	private static final Stroke middleLineStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
	private static final Stroke roadStroke = new BasicStroke((float)Waypoint.radius);
	
	private LinkedList<Waypoint> waypoints;
	
	public Circuit() {
		waypoints = new LinkedList<Waypoint>();
	}

	public void draw(Graphics2D g2d) {

		Polygon circuit = new Polygon();

		for (Waypoint wp : waypoints) {
			circuit.addPoint((int) wp.getPosition().x, (int) wp.getPosition().y);
		}
		
		//Road
		g2d.setStroke(roadStroke);
		g2d.setPaint(Color.darkGray);
		g2d.draw(circuit);
		
		//Line
		g2d.setStroke(middleLineStroke);
		g2d.setPaint(Color.white);
		g2d.draw(circuit);
		
		g2d.setStroke(new BasicStroke()); //restore default stroke
		
		for (Waypoint wp : waypoints) {
			wp.draw(g2d);
		}
	}

	public LinkedList<Waypoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(LinkedList<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}

	public void addWaypoint(Waypoint wp) {
		waypoints.add(wp);
	}

	public boolean onRoad(Vehicle vehicle) {
		//TODO
		return true;
	}
}
