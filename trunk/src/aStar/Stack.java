package aStar;

import javax.vecmath.Point2d;

public class Stack {
	private Node waitList[];
	private int cpt;

	public Stack(int size) {
		waitList = new Node[size];
		cpt = -1;
	}

	public void push(Point2d p, double d) {

		int i = cpt;
		cpt++;

		if (i >= 0 && d > waitList[i].getDistance()) {
			while (i >= 0 && d > waitList[i].getDistance()) {
				waitList[i + 1] = waitList[i];
				i--;
			}
			waitList[i + 1] = new Node(p, d);
		} else {
			waitList[cpt] = new Node(p, d);
		}
	}

	public Node pop() {
		if (cpt >= 0) {
			cpt--;
			return waitList[cpt + 1];
		} else {
			return null;
		}
	}

	public void clear() {
		waitList = new Node[waitList.length];
		cpt = -1;
	}

}