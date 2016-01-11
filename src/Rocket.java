// Author: Aidan Fisher

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class Rocket extends Entity {
	public int fuel = 100;
	public boolean playerControlled;
	public Planet boundToPlanet = null;
	public double rotation; // Can be in entity when other stuff.. has rotation, (only other thing is planet right now)
	public double dRot;
	public int width = 12, height = 30; // Same with these (Must be divisible by 2)
	public double frictionMultiplier = 0.5;
	public boolean colliding = false;

	// x, y are defined as the *CENTER* of the rocket.

	public Rocket(boolean pC, double x, double y) {
		this.playerControlled = pC;
		this.x = x;
		this.y = y;
		rotationPoint = new Point.Double(x, y);
	}

	public boolean tick() {
		if (boundToPlanet == null) {
			colliding = false;

			boolean tick = super.tick();

			// Lower rotation velocity.
			if (colliding) {
				dRot *= 0.999;
			} else {
				dRot *= 0.9;
			}

			return tick;
		} else {
			return false;
		}
	}

	public void moveTick() {
		if (boundToPlanet == null) {
			rotation += dRot * Component.gameSpeed;
			rotation = Entity.confirmRotation(rotation);
			aroundRotationPoint();
			super.moveTick();
		} else {
			setXYonBoundPlanet();
		}
	}

	public void moveForward(double d) {
		dX += Math.cos(rotation + Math.PI / 2) * d;
		dY += Math.sin(rotation + Math.PI / 2) * d;
	}

	public void bindToPlanet(Planet planet, double rot) {
		boundToPlanet = planet;
		rotation = rot + Math.PI / 2;
		setXYonBoundPlanet();
	}

	private void aroundRotationPoint() {
		double x = this.x;
		double y = this.y;
		// As to preserve the x and y for the equation tampers with x, but is still required for y.
		this.x = Math.cos(this.dRot * Component.gameSpeed) * (x - rotationPoint.x) - Math.sin(this.dRot * Component.gameSpeed) * (y - rotationPoint.y) + rotationPoint.x;
		this.y = Math.sin(this.dRot * Component.gameSpeed) * (x - rotationPoint.x) + Math.cos(this.dRot * Component.gameSpeed) * (y - rotationPoint.y) + rotationPoint.y;

		// Also add tdx and tdy values to match its current speed:
	}

	public void setXYonBoundPlanet() {
		x = boundToPlanet.x + Math.cos(rotation) * (boundToPlanet.radius + height / 2);
		y = boundToPlanet.y + Math.sin(rotation) * (boundToPlanet.radius + height / 2);
	}

	public void unBindFromPlanet() {
		this.dX = boundToPlanet.dX;
		this.dY = boundToPlanet.dY;
		this.gX = 0; // In case gravity is set to something iffy
		this.gY = 0;
		boundToPlanet = null;
	}

	public Rectangle getRect() {
		return new Rectangle((int) x - width, (int) y - height, width * 2, height * 2); // Approximation distance
	}

	public Rectangle.Double getRectExact() {
		return new Rectangle.Double(x - width / 2.0, y - height / 2.0, width, height);
	}

	public Rectangle getGravityRect() {
		return new Rectangle((int) x, (int) y, 0, 0);
	}

	protected boolean collisionCheck(Planet planet) {

		Rectangle.Double rect = this.getRectExact();

		double unrotatedCircleX = Math.cos(-this.rotation) * (planet.x - x) - Math.sin(-this.rotation) * (planet.y - y) + x;
		double unrotatedCircleY = Math.sin(-this.rotation) * (planet.x - x) + Math.cos(-this.rotation) * (planet.y - y) + y;

		// Closest point in the rectangle to the center of circle rotated backwards(unrotated)
		double closestX, closestY;

		// Find the unrotated closest x point from center of unrotated circle
		if (unrotatedCircleX < rect.x)
			closestX = rect.x;
		else if (unrotatedCircleX > rect.x + rect.width)
			closestX = rect.x + rect.width;
		else
			closestX = unrotatedCircleX;

		// Find the unrotated closest y point from center of unrotated circle
		if (unrotatedCircleY < rect.y)
			closestY = rect.y;
		else if (unrotatedCircleY > rect.y + rect.height)
			closestY = rect.y + rect.height;
		else
			closestY = unrotatedCircleY;

		double distance = distance(unrotatedCircleX, unrotatedCircleY, closestX, closestY);

		if (distance < planet.radius) {
			colliding = true;

			// Rotate closestX and closestY to "normal" rotation.
			double cX = Math.cos(this.rotation) * (closestX - x) - Math.sin(this.rotation) * (closestY - y) + x;
			double cY = Math.sin(this.rotation) * (closestX - x) + Math.cos(this.rotation) * (closestY - y) + y;

			// Move away from planet:
			double dir = Math.atan2(planet.y - y, planet.x - x);
			double xDist = Math.cos(dir) * (planet.radius - distance);
			double yDist = Math.sin(dir) * (planet.radius - distance);

			x -= xDist;
			y -= yDist;

			cX -= xDist;
			cY -= yDist;

			// Rotational logic:

			// Direction of pull (gravity usually)
			double m1 = (dY - planet.dY) / (dX - planet.dX);
			double b1 = cY - m1 * cX;

			// Center of gravity:
			double m2 = -1 / m1;
			double b2 = y - m2 * x;

			// Point of intersection:
			double iX = (b2 - b1) / (m1 - m2);
			double iY = m1 * iX + b1;

			double relativeDirection = Math.atan2(dY - planet.dY, dX - planet.dX);

			// Distance and rotation between the 2 points:
			double pRot = Math.atan2(iY - y, iX - x);

			relativeDirection = Entity.confirmRotation(relativeDirection);
			pRot = Entity.confirmRotation(pRot);

			if (pRot < -Math.PI / 2 && relativeDirection > Math.PI / 2) {
				pRot += Math.PI * 2;
			} else if (relativeDirection < -Math.PI / 2 && pRot > Math.PI / 2) {
				relativeDirection += Math.PI * 2;
			}
			int right;
			if (relativeDirection - pRot < 0) {
				// Left
				right = 1;
			} else {
				// Right
				right = -1;
			}
			double pDist = distance(iX, iY, x, y);

			rotationPoint = new Point.Double(cX, cY);

			double relativeSpeed = Math.sqrt((dX - planet.dX) * (dX - planet.dX) + (dY - planet.dY) * (dY - planet.dY));
			dRot = (right * (pDist / 500) * relativeSpeed);

			// Set dx and dy to planet: (bounce)
			bounceOff(planet, dir, relativeSpeed, relativeDirection);
			//dX = planet.dX;
			//dY = planet.dY;
			return true;
		}
		return false;
	}

	public void bounceOff(Planet p, double hitRotation, double relativeSpeed, double relativeDirection) {
		double newDirection = (Math.PI + hitRotation * 2) - relativeDirection;

		if (relativeSpeed < 1.0 / frictionMultiplier) {
			dX = p.dX;
			dY = p.dY;
		} else {
			dX = p.dX + Math.cos(newDirection) * relativeSpeed * frictionMultiplier;
			dY = p.dY + Math.sin(newDirection) * relativeSpeed * frictionMultiplier;
		}
	}

	public void render(Graphics2D g) {
		g.rotate(rotation, screenX(), screenY());
		g.setColor(new Color(255, 0, 0));
		g.fillRect(screenX() - (int) (width / 2.0 * Component.zoomLevel), screenY() - (int) (height / 2.0 * Component.zoomLevel), (int) (width * Component.zoomLevel),
				(int) (height * Component.zoomLevel));

		g.setColor(new Color(0, 255, 0));
		g.rotate(-rotation, screenX(), screenY());
		//g.fillRect((int) ((mX - Component.sX) * Component.zoomLevel), (int) ((mY - Component.sY) * Component.zoomLevel), 5, 5);
	}
}
