// Author: Aidan Fisher

public class Centre extends Building {

	// Stores items on the planet
	public int[] ore = new int[Quarry.NUM_ORES];

	// Also does other stuff to do with the planet

	public Centre(Planet planet, double rot, int w, int h) {
		super(planet, rot, w, h);
	}

	public String getName() {
		return "Centre";
	}

	public void addOre(int[] ore) {
		for (int i = 0; i < Quarry.NUM_ORES; i++) {
			this.ore[i] += ore[i];
		}
	}

}
