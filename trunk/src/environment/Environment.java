/**
 * 
 */
package environment;

import java.awt.Graphics2D;
import java.util.LinkedList;

import elements.Element;
import elements.Obstacle;
import elements.Vehicle;

/**
 * @author camille
 *
 */
public class Environment {
	
	/**
	 * The environment's name
	 */
	private String name;
	
	/**
	 * Associated {@link Circuit}
	 */
	private Circuit circuit;
	
	/**
	 * Associated {@link Element} list ({@link Vehicle}, {@link Obstacle}, ...)
	 */
	private LinkedList<Element> elements;
	
	
	public Environment() {
		super();
		elements = new LinkedList<Element>();
	}


	public void draw(Graphics2D g2d){
		
		/* Drawing the circuit */
		circuit.draw(g2d);
		
		/* Drawing the element's */
		for(Element elem : elements){
			elem.draw(g2d);
		}
	}
	
	/**
	 * Updates all the {@link Environment} elements
	 */
	public void update(){
		for(Element elem : elements){
			elem.update(this);
		}
	}

	public void addElement(Element e){
		elements.add(e);
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}


	public Circuit getCircuit() {
		return circuit;
	}


	public void setCircuit(Circuit circuit) {
		this.circuit = circuit;
	}


	public LinkedList<Element> getElements() {
		return elements;
	}


	public void setElements(LinkedList<Element> elements) {
		this.elements = elements;
	}
}
