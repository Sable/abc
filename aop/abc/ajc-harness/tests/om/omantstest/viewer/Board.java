
package viewer;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import model.World;

/** graphical representation of the grid of hexagons */
public class Board extends Canvas {

	private World world;
	
	private Hexagon[][] grid;
	
	private int maxUnitIncrement = 1;
	
	public Board() {
		super();
		this.world = World.v();
		reset();
	}
	
	public Hexagon getHexagon(int x, int y) {
		return grid[x][y];
	}
	
	/** reset the board when the world has changed */
	public void reset() {
		grid = new Hexagon[world.getMaxX()][world.getMaxY()];
		for (int y = 0; y < world.getMaxY(); y++)
			for (int x=0; x < world.getMaxX(); x++) {
				grid[x][y] = new Hexagon(world.getCell(x,y));
			}
		this.wsize = new Dimension((int) (world.getMaxX() * Hexagon.H_WIDTH + 2.0 * Hexagon.BORDER),
		                                    (int) ((world.getMaxY() /2 + world.getMaxY() % 2) * Hexagon.V_HEIGHT + 
		                                             (world.getMaxY() / 2 * Hexagon.H_SIDE + 2.0 * Hexagon.BORDER)));
	    setDirty();
	}

	public void render(Graphics g) {
		for (int y = 0; y < world.getMaxY(); y++)
					for (int x=0; x < world.getMaxX(); x++) {
						grid[x][y].render(g);
					}
	}
	
	private boolean dirty = true;
	private Dimension size, wsize;
	
	public synchronized void paint(Graphics g0) {
		render(g0);
	}
	
	public void update(Graphics g) { paint(g); }
	
	public synchronized void setDirty() { dirty = true; repaint(); }
	
	public Dimension getPreferredSize() {
		return wsize;
	}
	
}
