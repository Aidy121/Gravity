// Author: Aidan Fisher

import java.awt.*;

public class Element {

	public static final int DEFAULT = 0; // MODE
	public static final int MOUSE_OVER = 1; // MODE
	public static final int PRESSED = 2; // MODe

	public static final int LABEL = 0; // ACTION
	public static final int BUTTON = 1; // ACTION
	public static final int SLIDER = 2; // ACTION

	public static final int SLIDER_WIDTH = 14;

	public static double[][] sliderBarRanges = { { 0, 0.1 } };

	public int mode = DEFAULT;

	public Color color;

	public int action;

	public Gui parent = null;

	public int x, y;

	public int width, height;

	public String text;

	public String trueText;

	public Element(Gui gui, String text, int x, int y, int width, int height, int action) {
		this.parent = gui;
		this.text = text;
		this.x = x; // x is relative
		this.y = y; // y is relative 
		this.width = width;
		this.height = height;
		this.action = action;
		this.color = new Color(0, 0, 0);
		// Positions + Sizes can go above the gui size, and in such case, a "scrollbar" is created.
	}

	public Element(Gui gui, String text, int x, int y, int width, int height, int action, Color color) {
		this(gui, text, x, y, width, height, action);
		this.color = color;
	}

	public boolean pointInElement(Point p) {
		if (action == BUTTON) {
			return p.x > parent.x + x && p.y > parent.y + y && p.x < parent.x + x + width && p.y < parent.y + y + height;
		} else if (action == SLIDER) {
			return p.x > parent.x + x + getScrollValue() && p.y > parent.y + y && p.x < parent.x + x + getScrollValue() + SLIDER_WIDTH && p.y < parent.y + y + height;
		} else {
			return false;
		}
	}

	public void onClick() {
		if (action == BUTTON) {
			if (parent.title.equals("Buildings")) {
				Component.player.placingBuilding = text;
			}
		} else if (action == SLIDER) {

		}
	}

	public int getScrollValue() {
		if (text.equals("maxIncreaseRate")) {
			return (int) (((((ShieldGenerator) parent.associatedBuilding).maxIncreaseRate - sliderBarRanges[0][0]) / (double) (sliderBarRanges[0][1] - sliderBarRanges[0][0]))
					* (width - SLIDER_WIDTH * 3) + SLIDER_WIDTH);
		}
		return 0;
	}

	public void setScrollValue(double xPos) {
		if (text.equals("maxIncreaseRate")) {
			((ShieldGenerator) parent.associatedBuilding).maxIncreaseRate = setSpecificScrollValue(xPos, sliderBarRanges[0][0], sliderBarRanges[0][1]);
		}
	}

	public double setSpecificScrollValue(double xPos, double min, double max) {
		double returnVal = (xPos - (parent.x + x + SLIDER_WIDTH * 1.5)) / (double) (width - SLIDER_WIDTH * 3) * (max - min) + min;
		if (returnVal < min) {
			returnVal = min;
		} else if (returnVal > max) {
			returnVal = max;
		}
		return returnVal;
	}

	private String interpretText(String text) {
		if (text.startsWith("SET:") && text.substring(4, 7).equals("Ore")) {
			int i = 0;
			try {
				i = Integer.parseInt(text.substring(8, 9));
			} catch (Exception e) {
				System.out.println("Integer (Element) " + e);
			}
			return Component.player.onPlanet.centre.ore[i] + text.substring(10);
		}

		return text;
	}

	public void render(Graphics2D g) {

		trueText = interpretText(text);

		if (parent.style == Gui.STYLE_B) {
			g.setColor(new Color(200, 200, 0, 90));
		} else if (action == BUTTON) {
			if (mode == DEFAULT) {
				g.setColor(new Color(181, 89, 49, 90));
			} else if (mode == MOUSE_OVER) {
				g.setColor(new Color(211, 119, 79, 90));
			} else if (mode == PRESSED) {
				g.setColor(new Color(151, 59, 19, 90));
			}
		} else {
			g.setColor(new Color(181, 89, 49, 90));
		}

		g.fillRect(parent.x + x, parent.y + y, width, height);
		g.setColor(new Color(0, 0, 0));
		g.drawRect(parent.x + x, parent.y + y, width, height);

		if (action == LABEL || action == BUTTON) {

			g.setColor(color); // Text color

			int height = this.height - 6;

			g.setFont(new Font("Impact", Font.PLAIN, height)); // Maximum size (y)

			int width = g.getFontMetrics().stringWidth(trueText);

			if (width > this.width - 10) {
				height = (int) ((double) height * (this.width - 10.0) / width);
				g.setFont(new Font("Impact", Font.PLAIN, height)); // Maximum size (x)
				width = g.getFontMetrics().stringWidth(trueText);
			}

			int x = this.x + (this.width / 2) - width / 2 + 1;

			g.drawString(trueText, parent.x + x, parent.y + y + height + (this.height - height) / 2 - 1);
		} else if (action == SLIDER) {

			g.setStroke(new BasicStroke(3));
			g.setColor(new Color(0, 0, 0, 190));

			g.drawLine(parent.x + x + (int) (SLIDER_WIDTH * 1.5), parent.y + y + height / 2, parent.x + x + width - (int) (SLIDER_WIDTH * 1.5), parent.y + y + height / 2);

			g.setColor(new Color(181, 89, 49));

			g.fillRect(parent.x + x + getScrollValue(), parent.y + y + 5, SLIDER_WIDTH, height - 10);
			g.setColor(new Color(0, 0, 0));
			g.drawRect(parent.x + x + getScrollValue(), parent.y + y + 5, SLIDER_WIDTH, height - 10);

		}
	}
}
