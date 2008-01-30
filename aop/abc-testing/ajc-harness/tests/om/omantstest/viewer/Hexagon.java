
package viewer;
import java.awt.Color;
import java.awt.Graphics;
import model.Cell;

/** graphical representation of a cell.
 * modified from Hexagon code by Clark Verbrugge.
 */
public class Hexagon {
		
	/** length of a side of a hexagon */
	static final double H_SIDE = 4.5;  // length of a hex side
	/** horizontal width of a hexagon */ 
	static final double H_WIDTH = (H_SIDE*1.7320508);  // horiz. width
	/** vertical height of a hexagon */
	static final double V_HEIGHT = 2.0*H_SIDE;
	/** half the horizontal width of a hexagon */
	static final double H_WIDTH2 = (H_WIDTH/2.0);
	/** size of peak of a hexagon */
	static final double H_PEAK = (H_SIDE/2.0);
	/** size of border of grid */
	static final double BORDER = (H_WIDTH+3.0);

	/** x-coordinates of vertices.
		 * orientation of the hexagon is with point-up, so vertices are at
		 * 4 o'clock, 2 o'clock, 12 o'clock, etc.  The 0-vertex is at 4 o'clock,
		 * and order is counter-clockwise. */
	private double x[];
	/** x-coordinates of vertices.
		 * orientation of the hexagon is with point-up, so vertices are at
		 * 4 o'clock, 2 o'clock, 12 o'clock, etc.  The 0-vertex is at 4 o'clock,
		 * and order is counter-clockwise. */
	private double y[];
	/** centre of hexagon */
	private double cx,cy;  

    /** integer coordinates for drawing */
	private int px[]=new int[7],py[] = new int[7];
	
	/** the cell that this hexagon represents */
	private Cell cell; 
	
    /** the ant that this hexagon contains, if any */
	private Bug bug = new Bug();
	 
	/** create a Hexagon. code by Clark Verbrugge */
	public Hexagon(int ax,int ay) {
		  double eks,why;

		  x = new double[6];
		  y = new double[6];

		  if (ay%2!=0) eks = H_WIDTH2+BORDER;
		  else eks = BORDER;
		  why = BORDER+ay * (H_PEAK+H_SIDE)+H_PEAK;

		  x[3] = eks + ax * H_WIDTH; 	y[3] = why;
		  x[2] = x[3] + H_WIDTH2;    	y[2] = y[3] - H_PEAK;
		  x[1] = x[3] + H_WIDTH;     	y[1] = y[3];
		  x[0] = x[1];               			y[0] = y[1] + H_SIDE;
		  x[5] = x[2];               			y[5] = y[3] + H_SIDE + H_PEAK;
		  x[4] = x[3];               			y[4] = y[0];
		  
		  cx = (x[0] + x[1] + x[2] + x[3] + x[4] + x[5]) / 6.0;
		  cy = (y[0] + y[1] + y[2] + y[3] + y[4] + y[5]) / 6.0;
	}
	   
	 public Hexagon(Cell c) {
	   		this(c.getX(),c.getY());
	   		cell = c;
			String s;
			for (int i=0;i<6;i++) {
		   		px[i] = (int)(x[i]);
		   		py[i] = (int)(y[i]);
			}
			px[6] = px[0]; py[6] = py[0];
	   }
	   
    /** draw a hexagon. This has been carefully written so there is no heap turnover */
	public void render(Graphics g) {
	   boolean filled = false;
	   if (cell.getRocky()) {
		  g.setColor(Color.black);
		  g.fillPolygon(px,py,7);
		  filled = true;
	   } 
	   if (cell.getFood() > 0) {
			g.setColor(Color.green);
			g.fillPolygon(px,py,7);
			filled = true;
	   }	  
	   if (!filled && cell.isAntHill(model.Color.BLACK)) {
			g.setColor(Color.lightGray);
			g.fillPolygon(px,py,7);
			filled = true;
	   }
	   if (!filled && cell.isAntHill(model.Color.RED)) {
			g.setColor(Color.pink);
			g.fillPolygon(px,py,7);
			filled = true;
	   }
	   if (!filled) {
	   		g.setColor(Color.white);
	   		g.fillPolygon(px,py,7);
	   }
	   g.setColor(Color.black);
	   g.setPaintMode();
	   g.drawPolygon(px,py,7);
	   g.setColor(Color.black);
	   if (cell.hasAnt()) {
			bug.set(cell.getAnt(),cx,cy,H_SIDE);
	   		bug.render(g);
	   }
	}


}
