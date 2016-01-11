// Author: Aidan Fisher

import java.awt.*;
import java.util.*;

public class Gui {

	public static final int STYLE_A = 0;
	public static final int STYLE_B = 1;
	public static final int STYLE_C = 2; // Reserved for "Building" GUI types

	public int style = STYLE_A;

	public static double hudSize = 1;

	public static final int EDGE_WIDTH = 12;
	public static final int ELEMENT_SPACING = 10;
	public static final int TOP_BAR_SIZE = 40; // Not part of the height

	// Grid placement for elements:
	public int gridX = 0;
	public int gridY = 0;

	public int x;
	public int y;
	public int width;
	public int height;

	public int gridWidth;
	public int gridHeight;

	public Dimension gridSize;
	public Dimension boxSize;

	public String title;

	public ArrayList<Element> elements = new ArrayList<Element>();

	public Building associatedBuilding = null;

	public Polygon clickable;

	public Gui(String title, int style, Dimension gridSize, Dimension boxSize, int x, int y, int w, int h) {
		this.title = title;
		this.style = style;
		this.gridSize = gridSize;
		// (1, 0) Vertical list of elements... (Must specify a height for 0)
		// (0, 1) Horizontal list of elements... (Must specify a width for 0)
		// (3, 3) Specified grid of elements...
		this.boxSize = boxSize; // Specified width OR height OR niether for each element. 
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		clickable = new Polygon();
		putOnScreen();

		setGridWidthHeight(); // Has to set again if resized
	}

	/**   Action is determined with the title and text */
	public void addElement(String text, int action) {

		int x = (gridWidth + ELEMENT_SPACING) * gridX + ELEMENT_SPACING;
		int y = (gridHeight + ELEMENT_SPACING) * gridY + ELEMENT_SPACING;

		elements.add(new Element(this, text, x, y, gridWidth, gridHeight, action));

		// Set new gridX and gridY:
		gridX++;
		if (gridX >= gridSize.width && gridSize.width != 0) {
			gridX = 0;
			gridY++; // Assumes that extra y elements are not added.
		}

	}

	/** Num elements assumed to be increasing width */
	public void addElement(String text, int numElements, Color textColor, int action) {
		int newWidth = (gridWidth + 10) * numElements - 10;

		int x = (gridWidth + ELEMENT_SPACING) * gridX + ELEMENT_SPACING;
		int y = (gridHeight + ELEMENT_SPACING) * gridY + ELEMENT_SPACING;

		elements.add(new Element(this, text, x, y, newWidth, gridHeight, action, textColor)); // New width only used here

		gridX += numElements; // Assume no mistake
		if (gridX >= gridSize.width && gridSize.width != 0) {
			gridX = 0;
			gridY++; // Assumes that extra y elements are not added.
		}

	}

	public void setGridWidthHeight() {
		if (boxSize.width == 0) {
			gridWidth = (this.width - ELEMENT_SPACING) / gridSize.width - ELEMENT_SPACING;
		} else {
			gridHeight = boxSize.width - ELEMENT_SPACING;
		}
		if (boxSize.height == 0) {
			gridWidth = (this.height - ELEMENT_SPACING) / gridSize.height - ELEMENT_SPACING;
		} else {
			gridHeight = boxSize.height - ELEMENT_SPACING;
		}
	}

	/** Puts the "box" on the screen closest to its old position. 
	 * EDGE_WIDTH indicates the minimum distance from edge of screen */
	public void putOnScreen() {
		if (x < EDGE_WIDTH) {
			x = EDGE_WIDTH;
		} else if (x > Component.size.width - EDGE_WIDTH - width) {
			x = Component.size.width - EDGE_WIDTH - width;
		}
		if (y < EDGE_WIDTH + TOP_BAR_SIZE) {
			y = EDGE_WIDTH + TOP_BAR_SIZE;
		} else if (y > Component.size.height - EDGE_WIDTH - height) {
			y = Component.size.height - EDGE_WIDTH - height;
		}
	}

	public boolean pointInGui(Point p) {
		// Bounding box: 
		// (Set clickable)
		clickable.reset();

		clickable.addPoint(x + width - 2, y + height - 2);
		clickable.addPoint(x + width - 2, y - TOP_BAR_SIZE + 60);

		// Curve:
		// A, B, C points
		clickable.addPoint(x + width - 6, y - TOP_BAR_SIZE + 20);
		clickable.addPoint(x + width - 14, y - TOP_BAR_SIZE + 14);
		clickable.addPoint(x + width - 20, y - TOP_BAR_SIZE + 6);

		clickable.addPoint(x + width - 60, y - TOP_BAR_SIZE + 2);

		// Symmetrical:
		clickable.addPoint(x + 60, y - TOP_BAR_SIZE + 2);

		// Curve:
		// C, B, A points
		clickable.addPoint(x + 20, y - TOP_BAR_SIZE + 6);
		clickable.addPoint(x + 14, y - TOP_BAR_SIZE + 14);
		clickable.addPoint(x + 6, y - TOP_BAR_SIZE + 20);

		clickable.addPoint(x + 2, y + 2);
		clickable.addPoint(x + 2, y + height - 2);

		return clickable.contains(p);
	}

	public static void closeAllBuildingGui() {
		for (int i = 0; i < Component.gui.size(); i++) {
			if (Component.gui.get(i).style == STYLE_C) {
				Component.gui.remove(i);
				i--;
			}
		}
	}

	public static void addBuildingGui(Building b, Point p) {
		closeAllBuildingGui();
		String name = b.getName();
		if (name == "Shield Generator") {
			Gui g = new Gui(name, Gui.STYLE_C, new Dimension(2, 0), new Dimension(0, 50), p.x - 100, p.y - 20, 350, 200);
			g.associatedBuilding = b;
			Component.gui.add(g);
			g.addElement("Power Level: ", Element.LABEL);
			g.addElement("maxIncreaseRate", Element.SLIDER);
		}
	}

	public void drawCorners(Graphics2D g) {
		g.drawImage(Component.corners, x, y - TOP_BAR_SIZE, x + 60, y - TOP_BAR_SIZE + 60, 0, 0, 60, 60, null);
		g.drawImage(Component.corners, x + width - 60, y - TOP_BAR_SIZE, x + width, y - TOP_BAR_SIZE + 60, 60, 0, 120, 60, null);
		g.drawImage(Component.corners, x, y + height - 20, x + 60, y + height, 0, 60, 60, 80, null);
		g.drawImage(Component.corners, x + width - 60, y + height - 20, x + width, y + height, 60, 60, 120, 80, null);
	}

	public void drawBody(Graphics2D g) {
		g.setColor(new Color(181, 89, 49, 190));
		g.fillRect(x + 60, y - TOP_BAR_SIZE, width - 120, 60);
		g.fillRect(x + 60, y + height - 20, width - 120, 18);
		g.fillRect(x + 2, y - TOP_BAR_SIZE + 60, 58, height + TOP_BAR_SIZE - 80);
		g.fillRect(x + width - 60, y - TOP_BAR_SIZE + 60, 58, height + TOP_BAR_SIZE - 80);
		g.fillRect(x + 60, y - TOP_BAR_SIZE + 60, width - 120, height + TOP_BAR_SIZE - 80);

		g.setStroke(new BasicStroke(3));
		g.setColor(new Color(181, 181, 181));
		g.drawLine(x + 60, y - TOP_BAR_SIZE + 1, x + width - 60, y - TOP_BAR_SIZE + 1);
		g.drawLine(x + 60, y + height - 3, x + width - 60, y + height - 3);
		g.drawLine(x + 3, y - TOP_BAR_SIZE + 60, x + 3, y + height - 20);
		g.drawLine(x + width - 3, y - TOP_BAR_SIZE + 60, x + width - 3, y + height - 20);
	}

	public void render(Graphics2D g) {

		if (title.equals("Center")) {
			if (Component.player.onPlanet != null) {
				if (Component.player.onPlanet.centre == null) {
					Component.gui.remove(this);
					return;
				}
			} else {
				Component.gui.remove(this);
				return;
			}
		}

		drawCorners(g);
		drawBody(g);

		g.setColor(new Color(0, 0, 0));
		g.setFont(new Font("Impact", Font.PLAIN, TOP_BAR_SIZE - 12));
		int width = g.getFontMetrics().stringWidth(title);

		int x = this.x + (this.width / 2) - width / 2;
		g.drawString(title, x + 6, y - 6);

		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).render(g);
		}
	}
}
