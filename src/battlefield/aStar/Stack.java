package battlefield.aStar;

import java.util.Hashtable;

import javax.vecmath.Point2d;

public class Stack {
	private Node waitList[];
	private Hashtable<Point2d, Node> nodes;
	private int cpt;

	public Stack(int size) {
		waitList = new Node[size];
		cpt = -1;
		nodes = new Hashtable<Point2d, Node>();
	}

	private void rebalance(){
		for(int i = 0; i < cpt; i++){
			if (waitList[i].getDistance() < waitList[i+1].getDistance()){
				Node temp = waitList[i+1];
				waitList[i+1] = waitList[i];
				waitList[i] = temp;
			}
		}
	}
	
	public void push(Point2d p, double d, Point2d parent) {

		if ( nodes.get(p) == null ){
			int i = cpt;
			cpt++;
			Node n = new Node(p, d, parent);
			nodes.put(p, n);
			if (i >= 0 && d > waitList[i].getDistance()) {
				while (i >= 0 && d > waitList[i].getDistance()) {
					waitList[i + 1] = waitList[i];
					i--;
				}
				waitList[i + 1] = n;
			} else {
				waitList[cpt] = n;
			}
		} else if (nodes.get(p).getDistance() > d) {
			Node n = nodes.get(p);
			n.setDistance(d);
			n.setParent(parent);
			rebalance();
		}
	}

	public Node pop() {
		if (cpt >= 0) {
			cpt--;
			nodes.remove(waitList[cpt + 1].getPoint());
			return waitList[cpt + 1];
		} else {
			return null;
		}
	}

	public void clear() {
		waitList = new Node[waitList.length];
		cpt = -1;
		nodes = new Hashtable<Point2d, Node>();
	}

}