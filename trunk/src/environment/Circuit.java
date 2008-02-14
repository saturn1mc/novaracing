/**
 * 
 */
package environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;

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
			wp.draw(g2d);
			
			g2d.setPaint(Color.black);
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
}
