// Author: Aidan Fisher
// Note, this version is quite unstable to run due to some "testing" features.
// https://www.youtube.com/watch?v=N7PLrh1VAWk (3rd gravity planet video, Gravity Planet: Week 2)

import java.applet.*;
import java.awt.*;
import java.awt.image.VolatileImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Component extends Applet implements Runnable {

	public static final int CAMERA_CATCH_UP = 10;

	public static double ticksPerSecond = 60;
	public static double gameTickCounter = 0;
	public static double gameSpeed = 1; // Should be.. (1, 0.5, 0.25, 0.125)
	public static int collisionTick = 1;
	public static int collisionTickCounter = 0;

	public static int tickCount = 0;

	static int width = 1600;
	static int height = 900;

	private static final long serialVersionUID = 1L;

	public static Dimension size = new Dimension(width, height);

	public static String name = "Gravity Planets";

	public static boolean isRunning = false;

	public static Point screenPos = new Point(-1, -1);

	public static Gui movingGui = null;
	public static Point mousePos = null;

	private Image screen;

	public static Image mars, corners;

	public static double sX = 0, sY = 0, goalSX = 0, goalSY = 0;

	public static double zoomLevel = 2; //higher = more zoomed in, lower = more zoomed out

	public static QuadTree quad; // For collision
	public static QuadTree quadGravity; // For gravity
	public static ArrayList<Planet> planets = new ArrayList<Planet>();
	public static Player player;
	public static ArrayList<Rocket> ships = new ArrayList<Rocket>();

	public static ArrayList<Gui> gui = new ArrayList<Gui>(); // Guis are sorted in the array by their Z value, (No variable stores such value)
	// --> New gui's always get brought to "front", and clicked gui's also do that as well.

	public static double goalUniversalRotation = -Math.PI / 2; // The "goal" rotation that is used for smoother movement
	public static double universalRotation = -Math.PI / 2; // The render rotation offset of the world.

	public static AudioClip collision;

	public static int expectedPlayerSX = size.width / 2, expectedPlayerSY = size.height / 2;

	public Component() {
		setPreferredSize(size);
		addKeyListener(new Listening());
		addMouseListener(new Listening());
		addMouseWheelListener(new Listening());
		try {
			collision = Component.newAudioClip(new URL("file:c:/Users/Aidan%20Fisher/workspace/Gravity/res/explosion.wav"));
			mars = ImageIO.read(new File("res/Mars.png"));
			corners = ImageIO.read(new File("res/Corners.png"));
		} catch (Exception e) {
		}
	}

	public void start() {

		quad = new QuadTree(0, new Rectangle(-5000000, -5000000, 11000000, 11000000), false);
		quadGravity = new QuadTree(0, new Rectangle(-5000000, -5000000, 11000000, 11000000), true);

		//planets.add(new Planet(50000, 50000, 10000));

		planets.add(new Planet(500000, 500000, new Random().nextInt(1) + 300, 0, 0));

		planets.get(0).addBuilding("Centre", 0, 50, 140, true);

		//Defining Objects
		for (int i = 0; i < 100; i++) {
			planets.add(new Planet(new Random().nextInt(1000000), new Random().nextInt(1000000), new Random().nextInt(900) + 100, Math.random() * 20 - 10, Math.random() * 20 - 10));
		}

		player = new Player(planets.get(0).x, planets.get(0).y, 0, 0); // Make player "in planet" so player just goes on planet.

		ships.add(new Rocket(true, 500000, 502000));

		//player.onRocket = ships.get(0); Uncomment Change to rocket mode. (Comment below)
		player.onPlanet = planets.get(0);

		gui.add(new Gui("Buildings", Gui.STYLE_A, new Dimension(2, 0), new Dimension(0, 40), 0, 0, 400, 220));

		gui.get(0).addElement("Quarry", Element.BUTTON);
		gui.get(0).addElement("Shield Generator", Element.BUTTON);
		gui.get(0).addElement("Centre", Element.BUTTON);
		gui.get(0).addElement("House", Element.BUTTON);

		gui.add(new Gui("Ore", Gui.STYLE_B, new Dimension(2, 0), new Dimension(0, 50), 1000, 200, 350, 200));

		gui.get(1).addElement("Zen Ore", 1, new Color(140, 0, 0), Element.LABEL);
		gui.get(1).addElement("SET:Ore,0+kg", Element.LABEL);

		gui.get(1).addElement("Alnov Ore", 1, new Color(0, 200, 0), Element.LABEL);
		gui.get(1).addElement("SET:Ore,1+kg", Element.LABEL);

		gui.get(1).addElement("Tenzin Ore", 1, new Color(0, 0, 200), Element.LABEL);
		gui.get(1).addElement("SET:Ore,2+kg", Element.LABEL);

		//Starting game loop
		isRunning = true;

		new Thread(this).start();
	}

	public void stop() {
		isRunning = false;
	}

	public static void main(String args[]) {

		Component component = new Component();

		JFrame frame = new JFrame();
		frame.add(component);
		frame.setTitle(name);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setIconImage(icon);

		component.start();
	}

	public void tick() {

		// Add asteroids in the vicinity of planets.get(0)
		if (Math.random() < 0.1) {
			double dir = Math.random() * Math.PI * 2;
			planets.add(new Planet(planets.get(0).x + Math.cos(dir) * 30000, planets.get(0).y + Math.sin(dir) * 30000, new Random().nextInt(15) + 15, planets.get(0).dX + Math.random() * 10 - 5,
					planets.get(0).dY + Math.random() * 10 - 5));
		}

		quad.clear();
		quadGravity.clear();
		for (int i = 0; i < planets.size(); i++) {
			quad.insert(planets.get(i));
			quadGravity.insert(planets.get(i));
		}

		for (int i = 0; i < planets.size(); i++) {
			if (planets.get(i).tick()) {
				i--;
			}
		}

		for (int i = 0; i < ships.size(); i++) {
			ships.get(i).tick();
		}

		// PLAYER's tick is *AFTER* all planet ticks
		player.tick();

		/*tickCount++;
		if (tickCount == 20) {
			tickCount = 0;
			for (int i = 0; i < planets.size(); i++) {
				Point.Double p = planets.get(i).calculateGravity();
				planets.get(i).gX = p.x;
				planets.get(i).gY = p.y;
			}
		}*/
	}

	public void moveTick() {
		if (Listening.nextZoom == 1) {
			Listening.zoomIn(5, Listening.zoomEvent);
		} else if (Listening.nextZoom == -1) {
			Listening.zoomOut(5, Listening.zoomEvent);
		}
		if (Listening.elementPressed != null && mousePos != null) {
			if (Listening.elementPressed.action == Element.SLIDER) {
				Listening.elementPressed.setScrollValue(mousePos.x);
			}
		}

		for (int i = 0; i < planets.size(); i++) {
			planets.get(i).moveTick();
		}

		for (int i = 0; i < ships.size(); i++) {
			ships.get(i).moveTick();
		}

		// PLAYER's tick is *AFTER* all planet ticks
		player.moveTick();
	}

	private void centerPlayer() {
		sX = player.x - expectedPlayerSX / zoomLevel;
		sY = player.y - expectedPlayerSY / zoomLevel;
	}

	/** Sets the screen to "catch up" to the player 
	 * Position does not do this.  */
	public void updateScreen() {
		//System.out.println(player.aX + " " + player.aY);

		// Move ScreenPos at the speed of the player's Displacement
		centerPlayer();

		if (player.onPlanet == null && player.onRocket == null) {
			goalUniversalRotation = -player.gravityDirection + Math.PI / 2;
		} else {
			goalUniversalRotation = -player.rotation - Math.PI / 2;
		}
		goalUniversalRotation = Entity.confirmRotation(goalUniversalRotation);
		universalRotation = Entity.confirmRotation(universalRotation);
		if (goalUniversalRotation < -Math.PI / 2 && universalRotation > Math.PI / 2) {
			goalUniversalRotation += Math.PI * 2;
		} else if (universalRotation < -Math.PI / 2 && goalUniversalRotation > Math.PI / 2) {
			universalRotation += Math.PI * 2;
		}
		universalRotation += ((goalUniversalRotation - Component.universalRotation) / CAMERA_CATCH_UP);
		universalRotation = 0;
	}

	public void render() {
		((VolatileImage) screen).validate(getGraphicsConfiguration());
		Graphics g = screen.getGraphics();
		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, size.width, size.height);
		mousePos = getMousePosition();
		Graphics2D g2 = (Graphics2D) g;

		// Non-render stuff:

		if (mousePos != null) {
			Listening.mouseSequenceCheck(mousePos);
		}

		updateScreen();

		// Render stuff:

		Building b = null;
		if (mousePos != null && player.placingBuilding != "") {
			Point.Double p = Listening.toGameCoords(mousePos);

			double direction = Math.atan2(Component.planets.get(0).y - p.y, Component.planets.get(0).x - p.x);
			direction += (Math.PI / 2);

			b = Component.planets.get(0).addBuilding(player.placingBuilding, direction, 40, 20, false);
		}

		// Everything must be rotated so the player is "right-side up"
		g2.rotate(universalRotation, expectedPlayerSX, expectedPlayerSY);

		for (int i = 0; i < planets.size(); i++) {
			if (b != null && Component.player.onPlanet == planets.get(i)) {
				b.render(g2);
			}
			planets.get(i).render(g2);
		}

		for (int i = 0; i < ships.size(); i++) {
			ships.get(i).render(g2);
		}

		player.render(g2);

		g.setColor(Color.WHITE);
		g.drawRect((int) ((quad.bounds.x - Component.sX) * Component.zoomLevel), (int) ((quad.bounds.y - Component.sY) * Component.zoomLevel), (int) ((quad.bounds.width) * Component.zoomLevel),
				(int) ((quad.bounds.height) * Component.zoomLevel));

		//QuadTree quad = Component.quadGravity;

		//renderQuad(g, quad);
		g2.rotate(-universalRotation, expectedPlayerSX, expectedPlayerSY);

		// Render hud:
		for (int i = gui.size() - 1; i >= 0; i--) { // Renders them the "opposite" way
			gui.get(i).render(g2);
		}

		g = getGraphics();
		//g2 = (Graphics2D) g;

		g.drawImage(screen, 0, 0, size.width, size.height, 0, 0, size.width, size.height, null);
		g.dispose();
	}

	public void renderQuad(Graphics g, QuadTree quad) {
		if (quad.nodes[0] != null) {
			for (int k = 0; k < quad.nodes.length; k++) {
				g.drawRect((int) ((quad.nodes[k].bounds.x - Component.sX) * Component.zoomLevel), (int) ((quad.nodes[k].bounds.y - Component.sY) * Component.zoomLevel),
						(int) ((quad.nodes[k].bounds.width) * Component.zoomLevel), (int) ((quad.nodes[k].bounds.height) * Component.zoomLevel));
				renderQuad(g, quad.nodes[k]);
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		screen = createVolatileImage(size.width, size.height);
	}

	public void run() {
		screen = createVolatileImage(size.width, size.height);
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / /*Just in case*/(double) ticksPerSecond;
		while (isRunning) {
			//screen = createVolatileImage(size.width, size.height);
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			while (unprocessed >= 1) {
				moveTick();
				gameTickCounter += gameSpeed;
				if (gameTickCounter >= 0.996) {
					gameTickCounter = 0;
					tick();
				}
				unprocessed -= 1;
			}
			{
				render();
				if (unprocessed < 1) {
					try {
						Thread.sleep((int) ((1 - unprocessed) * nsPerTick) / 1000000, (int) ((1 - unprocessed) * nsPerTick) % 1000000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}

/** Changelog Week 0 
 * 
 *  - 1 Building glitch that removed the building fixed
 *  - An image, (that looks like mars,) currently substitutes 
 *   					all planets for the old circle.
 *   (Image is implemented from my Height Map Gradient Program)
 *  - Smoother camera movement (rotation wise)
 *  - BUG: Large planet that player is on slows game load significantly!
 *  - Player now acts like a building when player is on a planet
 *  - Buildings can only be added if in valid location
 *  - Movable HUD with elements that can be clicked, (or not, based on preference)
 *  - (Hovering and click colors come from this.)
 *  - Buildings can be selected from a GUI to be placed
 *  - Ore can be shown in a GUI, and colored and multigrid elements
 *  						are now possible
 *  - Fixed glitch where building would not place due to existing 
 *                          in the render for some time
 *  - Holding CTRL will let player place multiple of 1 building,
 *                  otherwise resets to not placing a building.
 *  - Fixed zooming in / out glitch
 *  - Smoother zooming in and out, 
 *  		(no more graphical glitches due to zooming in / out)
 *  - Added a "shield distance" for planets (blue sky)
 *  - Planets will destroy buildings if they collide
 *  - Shields will lose the radius of a planet that hits.
 *  
 ** Changelog Week 1
 * 
 *  - Added slider bar
 *  - Buildings are now clickable and will open gui's.
 *  - Added rocket
 *  - Rocket will rotate when it hits the planet and fall
 *  						accordingly.
 *  - Rocket will also bounce when it hits. 
 *  - This is done relative to the planet. 
 *  - Fixed glitch where moving planets would cause rocket to continue bouncing.
 * 
 ** Changelog Week 2
 * 
 *  - Made it so "movement" is seperate from the "tick," and the game can be now
 *  			slowed down, but still maintains exact calculations and smoothness.
 * 
 * [ And then I worked on a bunch of silly proof of concept stuff for the next 6 months.
 */
