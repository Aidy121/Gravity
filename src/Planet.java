// Author: Aidan Fisher

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Planet extends Entity {

	public double frictionMultiplier = 0.9;
	protected long mass;
	public double radius;
	public double extendedRadius;
	public double shield;

	public int ore[];

	public ArrayList<Building> buildings = new ArrayList<Building>();
	public Centre centre = null; // Centre must be the first thing to be built

	public Planet(double x, double y, int radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		mass = (long) radius * (long) radius * (long) radius;
		spawnOre();
	}

	public Planet(double x, double y, int radius, double dX, double dY) {
		this(x, y, radius);
		this.dX = dX;
		this.dY = dY;
	}

	public boolean tick() {
		setMaxBuildingHeight();
		for (int i = 0; i < buildings.size(); i++) {
			buildings.get(i).tick();
		}

		if (buildings.size() >= 2) {
			alignBuildings();
		}

		if (updateRadius()) {
			return super.tick();
		} else {
			return false;
		}
	}

	private void setMaxBuildingHeight() {
		extendedRadius = shield;
		for (int i = 0; i < buildings.size(); i++) {
			if (buildings.get(i).height > extendedRadius) {
				extendedRadius = buildings.get(i).height;
			}
		}
		extendedRadius += radius;
	}

	/** Aligns the buildings so they don't intersect eachother. 
	 * (Only needed when planet is getting smaller, since code
	 * does not let planets to even be placed in invalid locations. */
	public void alignBuildings() {
		int loopCounter = 0;
		boolean aligned;
		boolean allMisaligned;
		while (true) {
			Collections.sort(buildings);
			aligned = true;
			allMisaligned = true;
			for (int i = 0; i < buildings.size(); i++) {
				int n = 0;
				if (i != buildings.size() - 1) {
					n = i + 1;
				}
				if (buildings.get(i).align(buildings.get(n), true)) {
					aligned = false;
				} else {
					allMisaligned = false;
				}
			}
			if (aligned) {
				// Everything is aligned
				break;
			}
			if (allMisaligned) {
				// Everything is misaligned
				autoRemoveBuilding();
				break;
			}
			loopCounter++;
			if (loopCounter > 5) {
				// Loop counter prevention..
				break;
			}
		}
	}

	/** Checks every building to see if building doesn't intersect */
	public boolean canAddBuilding(Building b) {
		for (int i = 0; i < buildings.size(); i++) {
			if (buildings.get(i).align(b, false)) {
				return false;
			}
		}
		return true;
	}

	/** Searches for the first quarry and removes it, 
	 *  otherwise removes anything but the centre, 
	 *  and otherwise removes the centre <- technically not possible */
	public void autoRemoveBuilding() {
		for (int i = 0; i < buildings.size(); i++) {
			if (buildings.get(i).getClass().equals(Quarry.class)) {
				removeBuilding(buildings.get(i));
				return;
			}
		}
		for (int i = 0; i < buildings.size(); i++) {
			if (!buildings.get(i).getClass().equals(Centre.class)) {
				removeBuilding(buildings.get(i));
				return;
			}
		}
		System.out.println("Removing Centre");
		buildings.remove(0);
	}

	public void removeBuilding(Building b) {
		if (centre == b) {
			//centre = null; // All other buildings will fail!
		}
		buildings.remove(b);
	}

	/** Adds a building to the planet */
	public Building addBuilding(String type, double rot, int w, int h, boolean addToPlanet) {
		Building b = null;
		if (type.equals("Quarry")) {
			b = new Quarry(this, rot, w, h);
		} else if (type.equals("Centre")) {
			b = new Centre(this, rot, w, h);
			centre = (Centre) b;
		} else if (type.equals("Shield Generator")) {
			b = new ShieldGenerator(this, rot, w, h);
		} else {
			System.out.println("Doesn't exist");
			return null;
		}
		b.setRotationUsed();
		if (canAddBuilding(b)) {
			if (addToPlanet) {
				buildings.add(b);
			}
			return b;
		}
		//Collections.sort(buildings);

		return null;
	}

	/** Spawns ore on the planet.  */
	private void spawnOre() {
		// "Basic" planet.

		// Initialize:
		ore = new int[Quarry.NUM_ORES];
		for (int i = 0; i < Quarry.NUM_ORES; i++) {
			ore[i] = 0;
		}

		// Set random ores:
		ore[0] = (int) (0.009 * mass);
		ore[2] = (int) (0.001 * mass);
	}

	/** Updates the radius of the planet to be ^1/3 of the mass. (Removes the planet if mass < 0) */
	private boolean updateRadius() {
		if (mass < 0) {
			// Remove planet
			Component.planets.remove(this);
			return false;
		}
		radius = Math.pow(mass, 1.0 / 3.0);
		return true;
	}

	/** Adds the ore from a planet to this one.
	 * (Other planet is assumed to be removed after this.)
	 */
	private void addOre(Planet planet) {
		for (int i = 0; i < ore.length; i++) {
			ore[i] += planet.ore[i];
		}
	}

	protected Rectangle getRect() {
		return new Rectangle((int) (x - extendedRadius), (int) (y - extendedRadius), (int) (extendedRadius * 2), (int) (extendedRadius * 2));
	}

	protected Rectangle getGravityRect() {
		return new Rectangle((int) (x - Math.pow(mass, 0.55)), (int) (y - Math.pow(mass, 0.55)), (int) (Math.pow(mass, 0.55) * 2), (int) (Math.pow(mass, 0.55) * 2));
	}

	/** Checks collision between this planet and the planet specified 
	 * and reacts to any collision that occurs.
	 * Collision is done through a distance check.
	 */
	protected boolean collisionCheck(Planet planet) {
		double distance = distance(x, y, planet.x, planet.y);

		if (distance < extendedRadius + planet.extendedRadius) {
			for (int i = 0; i < buildings.size(); i++) {
				if (planet.collidesWith(buildings.get(i))) {
					removeBuilding(buildings.get(i));
					break; // Whatever (less glitchiness, that's for sure)
				}
			}

			for (int i = 0; i < planet.buildings.size(); i++) {
				if (collidesWith(planet.buildings.get(i))) {
					planet.removeBuilding(planet.buildings.get(i));
					break; // Whatever (less glitchiness, that's for sure)
				}
			}
			if (distance < (radius + shield) + (planet.radius + planet.shield)) {
				double direction = Math.atan2(y - planet.y, x - planet.x);
				if (Component.planets.indexOf(this) == 0 || Component.planets.indexOf(planet) == 0) {
					Component.collision.play();
				}
				// Direction * distanceToBeMoved * percentageOfMass

				// NOMM!

				if ((mass > planet.mass ? mass : planet.mass) / ((double) mass + planet.mass) > 0.66) { // if bigger planet is over 2/3 of the size, *NOMM*
					if (mass > planet.mass) {
						mass += planet.mass;
						shield -= planet.radius;
						if (shield < 0) {
							shield = 0;
						}
						addOre(planet);
						Component.planets.remove(planet);
					} else {
						planet.mass += mass;
						shield -= radius;
						if (shield < 0) {
							shield = 0;
						}
						planet.addOre(this);
						Component.planets.remove(this);
					}
					return true;
				} else {
					x += Math.cos(direction) * (((radius + shield + planet.radius + planet.shield) - distance) * planet.mass / (mass + planet.mass));
					y += Math.sin(direction) * (((radius + shield + planet.radius + planet.shield) - distance) * planet.mass / (mass + planet.mass));
					planet.x -= Math.cos(direction) * (((radius + shield + planet.radius + planet.shield) - distance) * mass / (mass + planet.mass));
					planet.y -= Math.sin(direction) * (((radius + shield + planet.radius + planet.shield) - distance) * mass / (mass + planet.mass));

					double newDirection = (Math.PI + direction * 2) - this.direction;
					double newDirection2 = (Math.PI + direction * 2) - planet.direction;
					dX = Math.cos(newDirection) * speed * frictionMultiplier;
					dY = Math.sin(newDirection) * speed * frictionMultiplier;
					planet.dX = Math.cos(newDirection2) * planet.speed * planet.frictionMultiplier;
					planet.dY = Math.sin(newDirection2) * planet.speed * planet.frictionMultiplier;
				}
			}
		}
		return false;
	}

	/** Checks to see if this planet, collides with a building
	 * from a different planet */
	public boolean collidesWith(Building b) {

		Rectangle.Double rect = b.getRect();

		double unrotatedCircleX = Math.cos(-b.rotation) * (x - b.planet.x) - Math.sin(-b.rotation) * (y - b.planet.y) + b.planet.x;
		double unrotatedCircleY = Math.sin(-b.rotation) * (x - b.planet.x) + Math.cos(-b.rotation) * (y - b.planet.y) + b.planet.y;

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

		// Determine collision
		boolean collision = false;

		double distance = distance(unrotatedCircleX, unrotatedCircleY, closestX, closestY);
		if (distance < radius + shield) // Include shield
			collision = true; // Collision
		else
			collision = false;
		return collision;
	}

	/** Checks if planet is on screen, considering the fact the screen can be rotated.
	 * This is done clumsily, and simply widens it's search range in order to suffice
	 * to all possible rotations.
	 * 
	 * @return true if planet could be on screen
	 */
	private boolean onScreen() {
		int x1 = (int) ((x - extendedRadius - Component.sX) * Component.zoomLevel);
		int y1 = (int) ((y - extendedRadius - Component.sY) * Component.zoomLevel);
		int x2 = (int) ((x + extendedRadius - Component.sX) * Component.zoomLevel);
		int y2 = (int) ((y + extendedRadius - Component.sY) * Component.zoomLevel);

		// Assumes width > height!!
		return x1 < Component.size.width * 1.25 && y1 < Component.size.width * 1.25 && x2 > -Component.size.width * 0.25 && y2 > -Component.size.width * 0.25;
	}

	public void renderShield(Graphics2D g) {
		if (shield != 0) {
			g.setColor(new Color(50, 150, 250));
			g.fillOval((int) ((x - radius - shield - Component.sX) * Component.zoomLevel), (int) ((y - radius - shield - Component.sY) * Component.zoomLevel),
					(int) ((radius + shield) * 2 * Component.zoomLevel), (int) ((radius + shield) * 2 * Component.zoomLevel));
		}
	}

	/** Renders the planet and all things "connected" to it */
	public void render(Graphics2D g) {
		// If *ANY* of planet (including buildings on screen)

		if (onScreen()) {
			renderShield(g);
			for (int i = 0; i < buildings.size(); i++) {
				buildings.get(i).render(g);
			}
			if (Component.zoomLevel >= 0.001) {
				g.drawImage(Component.mars, (int) ((x - radius - Component.sX) * Component.zoomLevel), (int) ((y - radius - Component.sY) * Component.zoomLevel),
						(int) (radius * 2 * Component.zoomLevel), (int) (radius * 2 * Component.zoomLevel), null);
			} else {
				g.setColor(new Color(200, 100, 50));
				g.fillOval((int) ((x - radius - Component.sX) * Component.zoomLevel), (int) ((y - radius - Component.sY) * Component.zoomLevel), (int) (radius * 2 * Component.zoomLevel),
						(int) (radius * 2 * Component.zoomLevel));
			}

		}

		//g.setColor(new Color(255, 255, 255));
		//g.drawLine((int) ((x - Component.sX) * Component.zoomLevel), (int) ((y - Component.sY) * Component.zoomLevel), (int) ((x - Component.sX + gX * 1000) * Component.zoomLevel), (int) ((y
		//		- Component.sY + gY * 1000) * Component.zoomLevel));

		//g.setColor(new Color(200, 100, 50, 128));
		//g.fillOval((int) ((x - Math.pow(mass, 0.55) - Component.sX) * Component.zoomLevel), (int) ((y - Math.pow(mass, 0.55) - Component.sY) * Component.zoomLevel),
		//		(int) (Math.pow(mass, 0.55) * 2 * Component.zoomLevel), (int) (Math.pow(mass, 0.55) * 2 * Component.zoomLevel));
	}
}
