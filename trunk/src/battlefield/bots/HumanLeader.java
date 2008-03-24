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

		this.mouse = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				aim = new Point2d(e.getX(), e.getY());
				shoot = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				shoot = false;
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Point2d start = new Point2d(getPosition().x, getPosition().y);
				Point2d stop = new Point2d(e.getX(), e.getY());

				if (field.getSurface().canSee(start, stop)) {
					target = new Waypoint(stop);
				} else {
					currentPath = new Path(field.getSurface().solve(start, stop));

					if (currentPath != null && currentPath.isSolved()) {
						target = currentPath.getPoints().getFirst();
					}
				}
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
	}
}
