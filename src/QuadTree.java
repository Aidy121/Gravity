// Author: Aidan Fisher

import java.awt.*;
import java.util.*;

public class QuadTree {

	// Code used from http://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374

	private int MAX_OBJECTS = 5;
	private int MAX_LEVELS = 10;

	private int level;
	private ArrayList<Planet> objects;
	public Rectangle bounds;
	public QuadTree[] nodes;
	private boolean gravity;

	/*
	 * Constructor
	 */
	public QuadTree(int pLevel, Rectangle pBounds, boolean gravity) {
		level = pLevel;
		objects = new ArrayList<Planet>();
		bounds = pBounds;
		nodes = new QuadTree[4];
		this.gravity = gravity;
	}

	public void clear() {
		objects.clear();

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].clear();
				nodes[i] = null;
			}
		}
	}

	private void split() {
		int subWidth = (int) (bounds.getWidth() / 2);
		int subHeight = (int) (bounds.getHeight() / 2);
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();

		nodes[0] = new QuadTree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight), gravity);
		nodes[1] = new QuadTree(level + 1, new Rectangle(x, y, subWidth, subHeight), gravity);
		nodes[2] = new QuadTree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight), gravity);
		nodes[3] = new QuadTree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight), gravity);
	}

	public void insert(Planet pRect) {
		if (nodes[0] != null) {
			int index;
			if (gravity) {
				index = getIndex(pRect.getGravityRect());
			} else {
				index = getIndex(pRect.getRect());
			}

			if (index != -1) {
				nodes[index].insert(pRect);

				return;
			}

			objects.add(pRect);
			return;
		}
		objects.add(pRect);

		if (objects.size() >= MAX_OBJECTS && level < MAX_LEVELS) {
			split();

			int i = 0;
			while (i < objects.size()) {
				int index;
				if (gravity) {
					index = getIndex(objects.get(i).getGravityRect());
				} else {
					index = getIndex(objects.get(i).getRect());
				}
				if (index != -1) {
					nodes[index].insert(objects.get(i));
					objects.remove(i);
				} else {
					i++;
				}
			}
		}
	}

	private int getIndex(Rectangle pRect) {
		int index = -1;
		double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
		double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);

		// Object can completely fit within the top quadrants
		boolean topQuadrant = (pRect.getY() + pRect.getHeight() < horizontalMidpoint);
		// Object can completely fit within the bottom quadrants
		boolean bottomQuadrant = (pRect.getY() > horizontalMidpoint);

		// Object can completely fit within the left quadrants
		if (pRect.getX() + pRect.getWidth() < verticalMidpoint) {
			if (topQuadrant) {
				index = 1;
			} else if (bottomQuadrant) {
				index = 2;
			}
		}
		// Object can completely fit within the right quadrants
		else if (pRect.getX() > verticalMidpoint) {
			if (topQuadrant) {
				index = 0;
			} else if (bottomQuadrant) {
				index = 3;
			}
		}
		return index;
	}

	public ArrayList<Planet> retrieve(ArrayList<Planet> returnObjects, Rectangle pRect) {

		int index = getIndex(pRect);
		if (index != -1 && nodes[0] != null) {
			nodes[index].retrieve(returnObjects, pRect);
		}

		returnObjects.addAll(objects);

		return returnObjects;
	}

}