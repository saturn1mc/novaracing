/**
 * 
 */
package battlefield.piemenu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.vecmath.Point2d;

/**
 * @author cmaurice2
 *
 */
public abstract class PieItem {
	
	private PieMenu parent;
	
	private Image icon;
	private Color color;
	private String text;
	private MouseAdapter mouse;
	private boolean active;
	private Rectangle bbox;
	
	public PieItem(Image icon, Color color) {
		this.icon = icon;
		this.text = "";
		this.color = color;
		this.bbox = new Rectangle();
		
		initMouse();
	}
	
	public PieItem(Image icon, String text, Color color) {
		this.icon = icon;
		this.text = text;
		this.color = color;
		
		initMouse();
	}

	private void initMouse(){
		this.mouse = new MouseAdapter(){
			@Override
			public void mouseMoved(MouseEvent e) {
				setActive(bbox.contains(e.getX(), e.getY()));
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(active){
					action();
				}
			}
		};
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void drawAt(Graphics g, Point2d position){
		
		Graphics2D g2d = (Graphics2D)g;
		
		this.bbox.setLocation((int) (position.x - (parent.getItemsRadius() / 2.0d)), (int) (position.y - (parent.getItemsRadius() / 2.0d)));
		
		if (icon != null) {
			g2d.drawImage(icon, (int) (position.x - (parent.getItemsRadius() / 2.0d)), (int) (position.y - (parent.getItemsRadius() / 2.0d)), (int) parent.getItemsRadius(), (int) parent.getItemsRadius(), null);
		} else {
			g2d.setColor(color);
			g2d.fillOval((int) (position.x - (parent.getItemsRadius() / 2.0d)), (int) (position.y - (parent.getItemsRadius() / 2.0d)), (int) parent.getItemsRadius(), (int) parent.getItemsRadius());
		}
		
		if(active){
			g2d.setColor(Color.WHITE);
			g2d.drawRect(bbox.x, bbox.y, bbox.width, bbox.height);
			g2d.drawString(text, (int)position.x, (int)position.y);
		}
	}
	
	public Image getIcon() {
		return icon;
	}

	public void setIcon(Image icon) {
		this.icon = icon;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public PieMenu getParent() {
		return parent;
	}

	public void setParent(PieMenu parent) {
		this.parent = parent;
		this.bbox.setSize(parent.getItemsRadius(), parent.getItemsRadius());
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public MouseAdapter getMouse() {
		return mouse;
	}

	public void setMouse(MouseAdapter mouse) {
		this.mouse = mouse;
	}

	public abstract void action();
}
