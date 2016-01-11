// Author: Aidan Fisher

import java.awt.*;
import java.util.Random;

public class Player extends Entity {

	public String placingBuilding = "";

	public static double jumpPower = 13.85;
	public static double currHeight = 0;
	public boolean movingLeft = false;
	public boolean movingRight = false;
	public boolean movingForward = false;
	public boolean movingBackward = false;

	public int width = 4;
	public int height = 16;

	public int fuel = 100;

	public Rocket onRocket = null;

	// On planet acting as a building:
	public Planet onPlanet = null; // If on planet, acts like a building
	public double rotation = 0;

	public Player(double x, double y, double dX, double dY) {
		this.x = x;
		this.y = y;
		this.dX = dX;
		this.dY = dY;
	}

	/** Moves player according to Listening, and also calculates acceleration */
	public boolean tick() {

		// Must move the planet first:
		boolean tick = false;
		if (onPlanet == null && onRocket == null) {
			// Act as an entity
			tick = super.tick();
		}

		return tick;
	}

	public void moveTick() {
		if (onPlanet != null) {
			// Act as a building
			moveLeftRightOnPlanet(); // This is done here, so the player has more control during slo-mo

			// Jump physics:
			currHeight += dY * Component.gameSpeed;
			if (currHeight > 0) {
				dY -= (onPlanet.radius / 100) * Component.gameSpeed;
			} else {
				dY = 0;
				currHeight = 0;
			}

			// Set x and y, so the player is in the right location to be rendered
			x = onPlanet.x + Math.cos(rotation) * (onPlanet.radius + currHeight);
			y = onPlanet.y + Math.sin(rotation) * (onPlanet.radius + currHeight);
			rotation = Entity.confirmRotation(rotation);
		} else if (onRocket != null) {
			x = onRocket.x;
			y = onRocket.y;
			moveRocket(); // This is done here, so the player has more control during slo-mo
			rotation = onRocket.rotation + Math.PI / 2;
			rotation = Entity.confirmRotation(rotation);
		} else {
			super.moveTick();
		}
	}

	/** Moves the player according to "movingLeft" and "movingRight" */
	public void moveLeftRightOnPlanet() {
		if (movingLeft && !movingRight) {
			rotation -= (5.0 / onPlanet.radius) * Component.gameSpeed;
		} else if (!movingLeft && movingRight) {
			rotation += (5.0 / onPlanet.radius) * Component.gameSpeed;
		}
	}

	public void moveRocket() {
		if (movingLeft && !movingRight) {
			onRocket.dRot -= 0.003 * Component.gameSpeed;
		} else if (!movingLeft && movingRight) {
			onRocket.dRot += 0.003 * Component.gameSpeed;
		}
		if (movingForward && !movingBackward) {
			onRocket.moveForward(1 * Component.gameSpeed);
		} else if (!movingForward && movingBackward) {
			onRocket.moveForward(-1 * Component.gameSpeed);
		}
	}

	public Rectangle getRect() {
		return new Rectangle((int) x, (int) y, 0, 0);
	}

	public Rectangle getGravityRect() {
		return new Rectangle((int) x, (int) y, 0, 0);
	}

	/** Checks collision between the player and the planet specified
	 * Player's velocity is matched up with planet on collision, and
	 * is considered "onGround" for the next 5 ticks. The player can
	 * now jump and consistently move without fuel.
	 */
	public boolean collisionCheck(Planet planet) {
		double distance = distance(x, y, planet.x, planet.y);
		//double direction = Math.atan2(y - planet.y, x - planet.x);
		if (distance < planet.radius) {
			// Direction * distanceToBeMoved * percentageOfMass

			onPlanet = planet;
			dY = 0;

			// Stuff to be done if not "sticking" to planet

			//x += Math.cos(direction) * ((planet.radius - distance));
			//y += Math.sin(direction) * ((planet.radius - distance));

			// Match dX and dY..
			//dX = planet.dX;
			//dY = planet.dY;
			return true;
		}
		return false;
	}

	public void render(Graphics2D g) {
		if (onPlanet != null) {
			g.rotate(rotation, onPlanet.screenX(), onPlanet.screenY());
			g.setColor(new Color(0, 0, 255));
			g.fillRect(onPlanet.screenX() + (int) ((onPlanet.radius + currHeight) * Component.zoomLevel), onPlanet.screenY() - (int) (width / 2.0 * Component.zoomLevel),
					(int) (height * Component.zoomLevel), (int) (width * Component.zoomLevel));
			g.rotate(-rotation, onPlanet.screenX(), onPlanet.screenY());
		} else if (onRocket != null) {
			g.rotate(rotation, screenX(), screenY());
			g.setColor(new Color(0, 0, 255));
			g.fillRect(screenX() - (int) (height * Component.zoomLevel), screenY() - (int) (width / 2.0 * Component.zoomLevel), (int) (height * Component.zoomLevel),
					(int) (width * Component.zoomLevel));
			g.rotate(-rotation, screenX(), screenY());
		} else {
			g.rotate(gravityDirection, screenX(), screenY());
			g.setColor(new Color(0, 0, 255));
			g.fillRect(screenX() - (int) (height * Component.zoomLevel), screenY() - (int) (width / 2.0 * Component.zoomLevel), (int) (height * Component.zoomLevel),
					(int) (width * Component.zoomLevel));
			g.rotate(-gravityDirection, screenX(), screenY());
		}
	}
}
