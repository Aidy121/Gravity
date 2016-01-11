// Author: Aidan Fisher

import java.awt.*;
import java.awt.event.*;

public class Listening implements MouseListener, MouseWheelListener, KeyListener {
	public static Point pressLocation = new Point(-1, -1); //off screen = none
	public static Point currLocation = new Point(-1, -1); //off screen = none
	public static Point lastLocation = new Point(-1, -1); //off screen = none
	public static boolean mouseDown = false;
	public static boolean controlDown = false;
	public static Element elementPressed = null;

	public static int nextZoom = 0; // 1 = zoom in, -1 = zoom out
	public static MouseWheelEvent zoomEvent = null;

	public static void zoomIn(double zoomRate, MouseEvent e) {
		Component.sX += e.getX() / Component.zoomLevel / (1.0 + zoomRate);
		Component.sY += e.getY() / Component.zoomLevel / (1.0 + zoomRate);
		Component.zoomLevel = Component.zoomLevel * (1.0 + (1.0 / zoomRate));
		nextZoom = 0;
	}

	public static void zoomOut(double zoomRate, MouseEvent e) {
		Component.sX -= e.getX() / Component.zoomLevel * (1.0 / zoomRate);
		Component.sY -= e.getY() / Component.zoomLevel * (1.0 / zoomRate);
		Component.zoomLevel = Component.zoomLevel / (1.0 + 1.0 / zoomRate);
		nextZoom = 0;
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseDown = true;
			pressLocation = new Point(e.getX(), e.getY()); //nothing happens
			lastLocation = pressLocation;

			// Checks gui...
			for (int i = 0; i < Component.gui.size(); i++) {
				if (Component.gui.get(i).pointInGui(pressLocation)) {
					// Bring to front
					Gui gui = Component.gui.get(i);
					Component.gui.remove(i);
					Component.gui.add(0, gui);
					for (int j = 0; j < gui.elements.size(); j++) {
						if ((gui.elements.get(j).action == Element.BUTTON || gui.elements.get(j).action == Element.SLIDER) && gui.elements.get(j).pointInElement(pressLocation)) {
							gui.elements.get(j).mode = Element.PRESSED;
							elementPressed = gui.elements.get(j);
							return; // Don't move gui if pressing a button..
						}
					}
					// Moving this gui:
					if (pressLocation.y < gui.y) { // (Only if clicked top bar)
						Component.movingGui = gui;
					}
					return;
				}
			}

		}
	}

	public void clickSequenceCheck(Point a, Point b) {
		if ((Math.abs(a.x - b.x) <= 100 && Math.abs(a.y - b.y) <= 100)) {
			// Click

			// Check HUD:

			for (int i = 0; i < Component.gui.size(); i++) {
				if (Component.gui.get(i).pointInGui(b)) {
					for (int j = 0; j < Component.gui.get(i).elements.size(); j++) {
						if (Component.gui.get(i).elements.get(j).action == Element.BUTTON && Component.gui.get(i).elements.get(j).pointInElement(b)
								&& elementPressed == Component.gui.get(i).elements.get(j)) {
							Component.gui.get(i).elements.get(j).onClick();
						}
					}
					return; // Mouse position *is not considered* if it has ended on hud after this
				}
			}

			if (Component.player.placingBuilding != "") { // If placing building currently
				Point.Double p = toGameCoords(b);

				double direction = Math.atan2(Component.planets.get(0).y - p.y, Component.planets.get(0).x - p.x);
				direction += (Math.PI / 2); // Off by 1/4... umm, could be fixed later

				// Add a building for now
				if (Component.planets.get(0).addBuilding(Component.player.placingBuilding, direction, 40, 20, true) != null) {
					if (!controlDown) {
						Component.player.placingBuilding = "";
					}
				}
			} else {

				Point.Double p = toGameCoords(b);

				// See if clicking a building:
				double direction = Math.atan2(Component.planets.get(0).y - p.y, Component.planets.get(0).x - p.x);
				//direction += (Math.PI / 2); // Off by 1/4

				double distance = Entity.distance(p.x, p.y, Component.planets.get(0).x, Component.planets.get(0).y);

				// Check every building:
				for (int i = 0; i < Component.player.onPlanet.buildings.size(); i++) {
					double newDirection = direction - Component.player.onPlanet.buildings.get(i).rotation;
					Point.Double newPos = new Point.Double(Component.planets.get(0).x - Math.cos(newDirection) * distance, Component.planets.get(0).y - Math.sin(newDirection) * distance);

					// Check if newPos is within building's rectangle
					if (Component.player.onPlanet.buildings.get(i).getRect().contains(newPos)) {
						Gui.addBuildingGui(Component.player.onPlanet.buildings.get(i), b);
					}
				}

			}

		}
	}

	public static void mouseSequenceCheck(Point p) {
		// Moves the current moving Gui.
		if (Component.movingGui != null && p != null) {
			Component.movingGui.x = p.x - Component.movingGui.width / 2;
			Component.movingGui.y = p.y + Gui.TOP_BAR_SIZE / 2;
			Component.movingGui.putOnScreen();
		}

		setAllElementsToDefault(Element.MOUSE_OVER);
		if (!mouseDown) {

			// Set the element currently being mouse overed to Mouse Over
			for (int i = 0; i < Component.gui.size(); i++) {
				if (Component.gui.get(i).pointInGui(p)) {
					for (int j = 0; j < Component.gui.get(i).elements.size(); j++) {
						if (Component.gui.get(i).elements.get(j).action == Element.BUTTON && Component.gui.get(i).elements.get(j).pointInElement(p)
								&& Component.gui.get(i).elements.get(j).mode != Element.PRESSED) {
							Component.gui.get(i).elements.get(j).mode = Element.MOUSE_OVER;
						}
					}
					break;
				}
			}
		}
	}

	public static void setAllElementsToDefault(int mode) {
		// Set all modes to Default
		for (int i = 0; i < Component.gui.size(); i++) {
			for (int j = 0; j < Component.gui.get(i).elements.size(); j++) {
				if (Component.gui.get(i).elements.get(j).mode == mode) {
					Component.gui.get(i).elements.get(j).mode = Element.DEFAULT;
				}
			}
		}
	}

	// The following methods are held in Listening currently, and should *always* be used when a "mouse coordinate" needs to be converted to a "game coordinate"
	// The opposite does not hold true currently, however.

	public static Point.Double toGameCoords(Point p) {
		// Must return a point that is properly rotated!
		double direction = Math.atan2(Component.player.screenY() - p.y, Component.player.screenX() - p.x);
		double distance = Entity.distance(p.x, p.y, Component.player.screenX(), Component.player.screenY());

		direction -= Component.universalRotation;
		Point.Double n = new Point.Double(0, 0);

		n.x = Component.player.screenX() - Math.cos(direction) * distance;
		n.y = Component.player.screenY() - Math.sin(direction) * distance;

		n.x = (n.x / Component.zoomLevel + Component.sX);
		n.y = (n.y / Component.zoomLevel + Component.sY);

		return n;
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseDown = false;
			clickSequenceCheck(pressLocation, e.getPoint());
			pressLocation = new Point(-1, -1);
			setAllElementsToDefault(Element.PRESSED);
			Component.movingGui = null;
			elementPressed = null;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			Component.player.placingBuilding = "";
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() > 0) {
			nextZoom = -1;
			zoomEvent = e;
		} else if (e.getWheelRotation() < 0) {
			nextZoom = 1;
			zoomEvent = e;
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_A) {
			Component.player.movingLeft = true;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			Component.player.movingRight = true;
		} else if (e.getKeyCode() == KeyEvent.VK_W) {
			Component.player.movingForward = true;
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			Component.player.movingBackward = true;
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			if (Component.player.onPlanet != null && Component.player.dY == 0) {
				Component.player.dY = Player.jumpPower;
			} else if (Component.player.onRocket != null) {
				if (Component.player.onRocket.colliding) {
					Component.player.onRocket.moveForward(10);
				}
			}
		} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			controlDown = true;
		} else if (e.getKeyCode() == KeyEvent.VK_L) {
			Component.ships.get(0).unBindFromPlanet();
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_A) {
			Component.player.movingLeft = false;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			Component.player.movingRight = false;
		} else if (e.getKeyCode() == KeyEvent.VK_W) {
			Component.player.movingForward = false;
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			Component.player.movingBackward = false;
		} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			controlDown = false;
		}
	}

	public void keyTyped(KeyEvent e) {
	}
}
