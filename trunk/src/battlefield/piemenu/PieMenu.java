/**
 * 
 */
package battlefield.piemenu;

import java.awt.Graphics;
import java.util.ArrayList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * @author cmaurice2
 *
 */
public class PieMenu {
	
	private Point2d position;
	private ArrayList<PieItem> items;
	private boolean visible;
	private int itemsRadius;
	
	public PieMenu(){
		this.position = new Point2d();
		this.items = new ArrayList<PieItem>();
	}
	
	public void draw(Graphics g){
		if(visible && (items.size() > 0)){
			
			Point2d itemPos = new Point2d();
			Vector2d dir = new Vector2d();
			Vector2d forward = new Vector2d(0, 1);
			
			double menuRadius = (items.size() * 2.0 * itemsRadius) / (2.0 * Math.PI);
			double rotationDelta = ((2.0d * Math.PI) / items.size());
			double rotation = 0;
			
			for(PieItem item : items){
				dir.set((forward.x * Math.cos(rotation)) - (forward.y * Math.sin(rotation)), (forward.y * Math.cos(rotation)) + (forward.x * Math.sin(rotation)));
				itemPos.set(this.position.x + (dir.x * menuRadius), this.position.y + (dir.y * menuRadius));
				
				item.drawAt(g, itemPos);
				
				rotation += rotationDelta;
			}
		}
	}
	
	public Point2d getPosition() {
		return position;
	}
	
	public void setPosition(int x, int y){
		this.position.set(x, y);
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getItemsRadius() {
		return itemsRadius;
	}

	public void setItemsRadius(int itemsRadius) {
		this.itemsRadius = itemsRadius;
	}

	public void addItem(PieItem item){
		items.add(item);
		item.setParent(this);
	}
	
	public void removeItem(PieItem item){
		items.remove(item);
		item.setParent(null);
	}
	
	public ArrayList<PieItem> getItems() {
		return items;
	}
}
