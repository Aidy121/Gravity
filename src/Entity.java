// Author: Aidan Fisher

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public abstract class Entity {
	public final static double GRAVITY = 0.001;
	public double x, y;
	public double dX = 0, dY = 0;
	public double gX = 0, gY = 0;
	public double direction = 0;
	public double speed = 0;
	public double gravityDirection = 0;
	public Point.Double rotationPoint = new Point.Double(0, 0); // The point at which rotation is "rotating around"

	public boolean tick() {
		dX += gX;
		dY += gY;
		direction = Math.atan2(dY, dX);
		speed = Math.sqrt(dX * dX + dY * dY);
		Point.Double p = calculateGravity();
		gX = p.x;
		gY = p.y;
		gravityDirection = Math.atan2(gY, gX);
		return allCollision();
	}

	public void moveTick() {
		x += dX * Component.gameSpeed;
		y += dY * Component.gameSpeed;
		rotationPoint.x += dX * Component.gameSpeed;
		rotationPoint.y += dY * Component.gameSpeed;
	}

	/** The MIDDLE x of the entity */
	public int screenX() {
		return (int) ((x - Component.sX) * Component.zoomLevel);
	}

	/** The MIDDLE y of the entity */
	public int screenY() {
		return (int) ((y - Component.sY) * Component.zoomLevel);
	}

	/** Returns the bounding rectangle for quadtree collision */
	protected Rectangle getRect() {
		System.out.println("Not to be used");
		return null;
	}

	/** Returns the "smallest" *Must* react to gravity range.
	 * Various planets will react further out, (larger planets more likely)
	 * Therefore, pass this range, effect should be either negatable
	 * or not effect the player.
	 */
	protected Rectangle getGravityRect() {
		System.out.println("Not to be used");
		return null;
	}

	public static double distance(double aX, double aY, double bX, double bY) {
		return Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2));
	}

	protected boolean collisionCheck(Planet planet) {
		System.out.println("Not to be used");
		return false;
	}

	/** Uses standard gravity calculations, (multiplied by GRAVITY) */
	public Point.Double calculateGravity() {
		Point.Double p = new Point.Double(0, 0);
		ArrayList<Planet> returnObjects = new ArrayList<Planet>();

		Component.quadGravity.retrieve(returnObjects, getGravityRect());

		for (int i = 0; i < returnObjects.size(); i++) {
			Planet planet = returnObjects.get(i);
			if (planet != this) {
				double distance = distance(x, y, planet.x, planet.y);
				double direction = Math.atan2(y - planet.y, x - planet.x);
				p.x -= Math.cos(direction) * GRAVITY * ((planet.mass) / (distance * distance));
				p.y -= Math.sin(direction) * GRAVITY * ((planet.mass) / (distance * distance));
			}
		}
		return p;
	}

	/** Does ALL collision. Planets are the only things that can be collided with */
	public boolean allCollision() {
		ArrayList<Planet> returnObjects = new ArrayList<Planet>();
		Component.quad.retrieve(returnObjects, this.getRect());

		for (int x = 0; x < returnObjects.size(); x++) {
			if (this != returnObjects.get(x)) {
				if (this.collisionCheck(returnObjects.get(x))) {
					return true;
				}
			}
		}
		return false;
	}

	public static double confirmRotation(double rotation) {
		if (rotation < -Math.PI) {
			rotation += (Math.PI * 2);
		} else if (rotation > Math.PI) {
			rotation -= (Math.PI * 2);
		}
		return rotation;
	}

}
