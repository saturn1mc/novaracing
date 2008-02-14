/**
 * 
 */
package environment;

import java.awt.Graphics2D;
import java.util.LinkedList;

import elements.Element;

/**
 * @author camille
 *
 */
public class Environment {
	private String name;
	private Circuit circuit;
	private LinkedList<Element> elements;
	
	public Environment() {
		super();
		elements = new LinkedList<Element>();
	}


	public void draw(Graphics2D g2d){
		
		circuit.draw(g2d);
		
		//TODO
		for(Element elem : elements){
			elem.draw(g2d);
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
