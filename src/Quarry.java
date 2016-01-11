// Author: Aidan Fisher

public class Quarry extends Building {

	public static final int NUM_ORES = 4;

	public int numWorkers = 1;
	public int level = 1;

	public int efficiency = 0; // % from 0 - 100 of ore finding

	public Quarry(Planet planet, double rot, int w, int h) {
		super(planet, rot, w, h);
	}

	public void tick() {
		super.tick();
		if (planet.centre != null) { // MUST have a centre to run.
			// Mine up ore
			mine(numWorkers * level * 50);
		}
	}

	public String getName() {
		return "Quarry";
	}

	public void mine(int amount) {
		// Ore detection level.. (Will just make the planet smaller)

		// Efficiency -> Ore Detection.

		double n = Math.random();

		double currMass = 0;
		double newMass = 0;
		for (int i = 0; i < NUM_ORES + 1; i++) {
			if (i == NUM_ORES) {
				// Mine up just the mass of the planet, (no return)
				planet.mass -= amount;
				break;
			} else {
				newMass += ((double) planet.ore[i] / planet.mass);
			}

			if (n >= currMass && n < newMass) {
				// Mine up this
				planet.ore[i] -= amount;
				planet.centre.ore[i] += amount;
				if (planet.ore[i] < 0) {
					planet.centre.ore[i] -= planet.ore[i];
					planet.ore[i] = 0; // Just mine the rest as "rock"
				}
				planet.mass -= amount;
				break;
			}

			currMass = newMass;
		}

	}
}
