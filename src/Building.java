// Author: Aidan Fisher

import java.awt.*;

public class Building implements Comparable<Building> {
	public static final double ROT_BETWEEN = 3; // The rotation between each building

	public double rotation;
	public Planet planet;
	public double rotationUsed = 0;

	public int width;
	public int height;

	public Building(Planet planet, double rot, int w, int h) {
		this.planet = planet;
		rotation = rot;
		width = w;
		height = h;
	}

	public void tick() {
		setRotationUsed(); // Has to be modified every tick because planet sizes change
	}

	/** Sets the range of rotation that the planet is currently taking up */
	public void setRotationUsed() {
		rotationUsed = Math.atan2(planet.y - (planet.y + planet.radius), planet.x - (planet.x - width / 2)) + Math.PI / 2;
	}

	/** Aligns a building on the planet so it doesn't intersect with the one nearby */
	public boolean align(Building b, boolean autoMove) {
		rotation = Entity.confirmRotation(rotation);
		b.rotation = Entity.confirmRotation(b.rotation);
		if (rotation < -Math.PI / 2 && b.rotation > Math.PI / 2) {
			rotation += Math.PI * 2;
		} else if (b.rotation < -Math.PI / 2 && rotation > Math.PI / 2) {
			b.rotation += Math.PI * 2;
		}

		if (autoMove) {
			if (b.rotation > rotation - (b.rotationUsed + ROT_BETWEEN / planet.radius + rotationUsed) && b.rotation <= rotation) {
				b.rotation = rotation - (b.rotationUsed + ROT_BETWEEN / planet.radius + rotationUsed);
				return true;
			} else {
				return false;
			}
		} else {
			return b.rotation > rotation - (b.rotationUsed + ROT_BETWEEN / planet.radius + rotationUsed) && b.rotation < rotation + (b.rotationUsed + ROT_BETWEEN / planet.radius + rotationUsed);
		}
	}

	public Rectangle.Double getRect() {
		// Returns the rectangle as if it was at 0 rotation
		return new Rectangle.Double(planet.x - width / 2.0, planet.y + planet.radius, width, height);
	}

	public String getName() {
		return "Building";
	}

	public void render(Graphics2D g) {

		g.rotate(rotation, planet.screenX(), planet.screenY());

		g.setStroke(new BasicStroke((int) Component.zoomLevel + 1));
		g.setColor(new Color(160, 160, 160));
		g.fillRect(planet.screenX() - (int) (width / 2.0 * Component.zoomLevel), planet.screenY() + (int) ((planet.radius) * Component.zoomLevel), (int) (width * Component.zoomLevel),
				(int) (height * Component.zoomLevel));
		g.setColor(new Color(200, 200, 200));
		g.drawRect(planet.screenX() - (int) (width / 2.0 * Component.zoomLevel), planet.screenY() + (int) ((planet.radius) * Component.zoomLevel), (int) (width * Component.zoomLevel),
				(int) (height * Component.zoomLevel));

		Polygon p = new Polygon();
		p.addPoint(planet.screenX(), planet.screenY());
		p.addPoint(planet.screenX() - (int) (width / 2.0 * Component.zoomLevel), planet.screenY() + (int) ((planet.radius) * Component.zoomLevel));
		p.addPoint(planet.screenX() + (int) (width / 2.0 * Component.zoomLevel), planet.screenY() + (int) ((planet.radius) * Component.zoomLevel));

		g.setColor(new Color(160, 100, 100));
		g.fillPolygon(p);
		g.setColor(new Color(200, 200, 200));
		g.drawPolygon(p);

		//g.setFont(new Font("Verdana", Font.PLAIN, 30));
		//g.setColor(new Color(0, 255, 0));
		//g.drawString(planet.buildings.indexOf(this) + "", planet.screenX(), planet.screenY() + (int) ((planet.radius) * Component.zoomLevel));

		g.rotate(-rotation, planet.screenX(), planet.screenY());

	}

	public int compareTo(Building b) {
		if (rotation > b.rotation) {
			return -1;
		} else if (b.rotation > rotation) {
			return 1;
		} else {
			System.out.println("Double");
			return 0; // Technically shouldn't happen
		}
	}
}
