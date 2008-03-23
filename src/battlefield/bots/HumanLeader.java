/**
 * 
 */
package battlefield.bots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.vecmath.Point2d;

import battlefield.BattleField;

/**
 * @author Camille
 *
 */
public class HumanLeader extends Leader {

	/**
	 * Mouse adapter for the Bot
	 */
	private MouseAdapter mouse;

	/**
	 * Keyboard adapter for the FBot
	 */
	private KeyAdapter keyboard;

	public HumanLeader(String name, Point2d position, Color color, int formation) {

		super(name, position, color, formation);

		this.mouse = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO
			}
		};

		this.keyboard = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_S) {
					// Square formation
					setFormationOrder(Leader.FORMATION_SQUARE);
				}

				if (e.getKeyCode() == KeyEvent.VK_L) {
					// Line formation
					setFormationOrder(Leader.FORMATION_LINE);
				}

				if (e.getKeyCode() == KeyEvent.VK_W) {
					// Wing formation
					setFormationOrder(Leader.FORMATION_WING);
				}

				if (e.getKeyCode() == KeyEvent.VK_C) {
					// Shield formation
					setFormationOrder(Leader.FORMATION_SHIELD);
				}
			}
		};
	}

	@Override
	public void draw(Graphics2D g2d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(BattleField env) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getRadius() {
		// TODO Auto-generated method stub
		return 0;
	}

	public MouseAdapter getMouse() {
		return mouse;
	}

	public KeyAdapter getKeyboard() {
		return keyboard;
	}
}
