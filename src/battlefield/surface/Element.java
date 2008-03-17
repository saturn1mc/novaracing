/**
 * 
 */
package battlefield.surface;

import java.awt.Graphics2D;

import javax.vecmath.Point2d;

import battlefield.BattleField;

/**
 * @author camille
 *
 */
public abstract class Element {
	
	/**
	 * The element's name (could be used as an identifier)
	 */
	protected String name;
	
	/**
	 * The element's absolute position in the window
	 */
	protected Point2d position;
	
	public Element(String name, Point2d position) {
		super();
		this.name = name;
		this.position = position;
	}

	/**
	 * Draws the element
	 * @param g2d
	 */
	public abstract void draw(Graphics2D g2d);
	
	/**
	 * Updates the element on each frame
	 * @param env
	 */
	public abstract void update(BattleField env);

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
	
	public double getRadius(){return 0.0d;}
}
