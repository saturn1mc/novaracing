/**
 * 
 */
package environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.LinkedList;

import elements.Vehicle;
import elements.Waypoint;

/**
 * @author camille
 *
 */
public class Circuit {
	
	private LinkedList<Waypoint> waypoints;
	
	public Circuit(){
		waypoints = new LinkedList<Waypoint>();
	}
	
	public void draw(Graphics2D g2d){
		for(Waypoint wp : waypoints){
			
			if(wp.getNext() != null){
				g2d.setPaint(Color.darkGray);
				
				Polygon p = new Polygon();
				p.addPoint((int)wp.getPosition().x, (int)(wp.getPosition().y + wp.getRadius()));
				p.addPoint((int)wp.getNext().getPosition().x, (int)(wp.getNext().getPosition().y + wp.getRadius()));
				p.addPoint((int)wp.getNext().getPosition().x, (int)(wp.getNext().getPosition().y - wp.getRadius()));
				p.addPoint((int)wp.getPosition().x, (int)(wp.getPosition().y - wp.getRadius()));
				
				g2d.fill(p);
			}
		}
		
		for(Waypoint wp : waypoints){
			
			wp.draw(g2d);
			
			g2d.setPaint(Color.white);
			g2d.drawLine((int)wp.getPosition().x, (int)wp.getPosition().y, (int)wp.getNext().getPosition().x, (int)wp.getNext().getPosition().y);
		}
	}

	public LinkedList<Waypoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(LinkedList<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}
	
	public void addWaypoint(Waypoint wp){
		waypoints.add(wp);
	}
	
	public boolean onRoad(Vehicle vehicle){
		//TODO
		return true;
	}
}
