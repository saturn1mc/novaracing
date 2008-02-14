/**
 * 
 */
package elements;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;

import environment.Environment;

/**
 * @author camille
 *
 */
public abstract class Element {
	
	protected String name;
	protected Point2d position;
	
	public Element(String name, Point2d position) {
		super();
		this.name = name;
		this.position = position;
	}

	public void draw(Graphics2D g2d){
		//TODO
	}
	
	public void update(Environment env){
		//TODO
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Point2d getPosition() {
		return position;
	}

	public void setPosition(Point2d position) {
		this.position = position;
	}
}
