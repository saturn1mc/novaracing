/**
 * 
 */
package game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import environment.Circuit;
import environment.Environment;

/**
 * @author camille
 *
 */
public class Nova extends JFrame {

	/**
	 * Generated SVUID
	 */
	private static final long serialVersionUID = 1L;
	
	private Environment environment;
	
	/**
	 * Default instance of {@link Nova}
	 */
	public Nova(){
		super("Nova Race");
		environment = new Environment();
		setPreferredSize(new Dimension(800, 600));
		setResizable(false);
	}
	
	@Override
	public void paintComponents(Graphics g) {
		super.paintComponents(g);
		environment.draw((Graphics2D)g);
	}
	
	public void loadTestRace(){
		Circuit circuit = new Circuit();
		//TODO
	}
	
	public static void main(String[] args) {
		Nova nova = new Nova();
		
		nova.loadTestRace();
		
		nova.setVisible(true);
	}
}
