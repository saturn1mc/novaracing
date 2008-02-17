/**
 * 
 */
package elements;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import environment.Environment;

/**
 * @author camille
 *
 */
public abstract class Element {
	
	protected String name;
	protected Point2d position;
	protected Vector2d forces;
	
	public Element(String name, Point2d position) {
		super();
		this.name = name;
		this.position = position;
		this.forces = new Vector2d(0, 0);
	}

	public void draw(Graphics2D g2d){}
	
	public void update(Environment env){}

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
	
	/*
	 * Influences
	 */
	public void effectOn(Element elem){
		affectBy(this);
	}
	
	public void affectBy(Element elem){}
}
