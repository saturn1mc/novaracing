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
import battlefield.aStar.Path;
import battlefield.bonuses.AmmoPoint;
import battlefield.bonuses.LifePoint;
import battlefield.piemenu.PieItem;
import battlefield.piemenu.PieMenu;
import battlefield.surface.Waypoint;

/**
 * @author Camille
 * 
 */
public class HumanLeader extends Leader {

	/**
	 * Leader's {@link BattleField}
	 */
	private BattleField field;

	/**
	 * Mouse adapter for the Bot
	 */
	private MouseAdapter mouse;

	/**
	 * Keyboard adapter for the FBot
	 */
	private KeyAdapter keyboard;

	/**
	 * Action menu for the leader
	 */
	private PieMenu actionMenu;

	/**
	 * Shooting indicator
	 */
	private boolean shoot;

	/**
	 * Shooting aim point
	 */
	private Point2d aim;

	public HumanLeader(BattleField env, String name, Point2d position, Color color, int formation) {

		super(name, position, color, formation);

		this.field = env;
		this.shoot = false;
		this.aim = new Point2d();

		this.mouse = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					aim.set(e.getX(), e.getY());
					shoot = true;
				}

				if (e.getButton() == MouseEvent.BUTTON3) {
					selectAction(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				shoot = false;
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				aim.set(e.getX(), e.getY());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				aim.set(e.getX(), e.getY());
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

		initActionMenu();
	}

	private void initActionMenu() {
		actionMenu = new PieMenu();
		actionMenu.setItemsRadius(20);
		// TODO add pie items

		// Item 1 : Move
		PieItem moveItem = new PieItem(null, Color.RED) {
			@Override
			public void action() {
				moveTo((int) getParent().getPosition().x, (int) getParent().getPosition().y);
			}

		};

		PieItem wingItem = new PieItem(null, Color.BLUE) {
			@Override
			public void action() {
				setFormationOrder(FORMATION_WING);
			}

		};

		PieItem squareItem = new PieItem(null, Color.GREEN) {
			@Override
			public void action() {
				setFormationOrder(FORMATION_SQUARE);
			}

		};

		PieItem shieldItem = new PieItem(null, Color.MAGENTA) {
			@Override
			public void action() {
				setFormationOrder(FORMATION_SHIELD);
			}

		};

		actionMenu.addItem(moveItem);
		actionMenu.addItem(wingItem);
		actionMenu.addItem(squareItem);
		actionMenu.addItem(shieldItem);
		
		actionMenu.setVisible(false);
	}

	private void selectAction(MouseEvent e) {
		actionMenu.setPosition(e.getX(), e.getY());
		actionMenu.setVisible(true);

		// TODO create pie menu items
		moveTo(e.getX(), e.getY()); // TODO
	}

	private void moveTo(int x, int y) {
		Point2d start = new Point2d(getPosition().x, getPosition().y);
		Point2d stop = new Point2d(x, y);

		if (field.getSurface().canSee(start, stop)) {
			target = new Waypoint(stop);
		} else {
			currentPath = new Path(field.getSurface().solve(start, stop));

			if (currentPath != null && currentPath.isSolved()) {
				target = currentPath.getPoints().getFirst();
			}
		}
	}

	@Override
	public void update(BattleField env) {

		enemy = enemyAtSight(env); // Enemy at sight for the followers

		if (shoot) {
			shootAt(field, aim);
		}

		if (target != null) {

			// TODO take the other elements effects into account
			// (collision, bonuses, obstacles, ...)
			updateForces();

			updateFuturePosition();
			computeTrajectoryCorrection(env);

			forward.set(velocity);
			forward.normalize();

			side.set((forward.x * Math.cos(Math.PI / 2.0d)) - (forward.y * Math.sin(Math.PI / 2.0d)), (forward.y * Math.cos(Math.PI / 2.0d)) + (forward.x * Math.sin(Math.PI / 2.0d)));
			side.normalize();

			/* Updating position */
			position.add(velocity);

			/* Updating sight */
			sight.reset();
			sight.addPoint((int) (position.x + (side.x * radius)), (int) (position.y + (side.y * radius)));
			sight.addPoint((int) (position.x - (side.x * radius)), (int) (position.y - (side.y * radius)));
			sight.addPoint((int) (futurePosition.x - (side.x * radius)), (int) (futurePosition.y - (side.y * radius)));
			sight.addPoint((int) (futurePosition.x + (side.x * radius)), (int) (futurePosition.y + (side.y * radius)));

			if (target.isReachedBy(this)) {
				target = target.getNext();
			}

			LifePoint lp = field.nearestLifePoint(this);
			if (lp.isReachedBy(this)) {
				lp.takeLife(this, 1 - this.getLife());
			}

			AmmoPoint ap = field.nearestAmmoPoint(this);
			if (ap.isReachedBy(this)) {
				ap.takeAmmo(this, this.getCurrentWeapon().maxAmmo());
			}
		}

		this.bbox.setLocation((int) (position.x - (radius / 2.0d)), (int) (position.y - (radius / 2.0d)));
	}

	public PieMenu getActionMenu() {
		return actionMenu;
	}

	public MouseAdapter getMouse() {
		return mouse;
	}

	public KeyAdapter getKeyboard() {
		return keyboard;
	}

	@Override
	public void draw(Graphics2D g2d) {

		super.draw(g2d);

		/* Its current human orders */
		if (target != null) {
			g2d.drawLine((int) position.x, (int) position.y, (int) target.getPosition().x, (int) target.getPosition().y);
		}

		/* Action menu */
		if (actionMenu.isVisible()) {
			g2d.translate(0, -BattleField.getInstance().getInsets().top);
			actionMenu.draw(g2d);
		}
	}
}
