// Author: Aidan Fisher

import java.awt.Graphics2D;

public class ShieldGenerator extends Building {

	public double shieldMultiplier = 300;

	public double maxIncreaseRate = 0.01; // this & the shield divider define the max shield

	public ShieldGenerator(Planet planet, double rot, int w, int h) {
		super(planet, rot, w, h);
	}

	public String getName() {
		return "Shield Generator";
	}

	public void tick() {
		super.tick();
		if (planet.centre != null) { // Must have a centre to run
			// Sets shield to wantedShieldLevel, at the cost of power.
			double shieldLoss = (planet.shield * planet.shield + planet.radius * planet.radius) * 0.0000001;
			planet.shield -= shieldLoss;
			if (planet.shield < 0) {
				planet.shield = 0; // No shield
			}

			// Requires ore[0] to work:

			if (planet.centre.ore[0] > maxIncreaseRate * shieldMultiplier) {
				planet.centre.ore[0] -= maxIncreaseRate * shieldMultiplier;
				planet.shield += maxIncreaseRate;
			} else {
				planet.shield += planet.centre.ore[0] / shieldMultiplier;
				planet.centre.ore[0] = 0;
			}
		}

	}

	public void render(Graphics2D g) {
		super.render(g);
	}

}
