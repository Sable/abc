
package viewer;

import model.Ant;
import java.awt.Graphics;
import java.awt.Color;
import java.lang.Math;

/** the graphical representation of an ant */
public class Bug {
    
    Ant ant;
    
    double cx;
    double cy;
    double width;
    
    double[] xs = new double[4];
    double[] ys = new double[4];
    
    int[] pxs = new int[5];
    int[] pys = new int[5];
    
    Color acolor;
    
    int foodx,foody,foodw;
           
    public void set(Ant a, double cx, double cy, double width) {
		ant = a;
		this.cx = cx;
		this.cy = cy;
		this.width = width;
		xs[0] = width/1.4; ys[0] = 0.0;
		xs[1] = -width/2; ys[1] = -width/2;
		xs[2] = -width/3; ys[2] = 0.0;
		xs[3] = -width/2; ys[3] = width/2;
		if (ant.getColor() == model.Color.BLACK) {
				acolor = Color.black;
		} else acolor = Color.red;
		double foodwidth = width/2;
		foodx = (int) (cx - foodwidth/2);
		foody = (int) (cy - foodwidth/2);
		foodw = (int) foodwidth;
    }
    
    private double rotX(double theta,double x,double y) {
    	return x * Math.cos(theta) - y * Math.sin(theta);
    }
    
    private double rotY(double theta, double x, double y) {
    	return y * Math.cos(theta) + x * Math.sin(theta);
    }
    
    public void orientate() {
    	for (int i=0; i < 4; i++) {
    		double theta = (Math.PI / 3.0) * ant.getDirection();
    		pxs[i] = (int) (rotX(theta,xs[i],ys[i]) + cx);	
    		pys[i] = (int) (rotY(theta,xs[i],ys[i]) + cy);
    	}
    	pxs[4] = pxs[0]; pys[4] = pys[0];
    }
    
    public void render(Graphics g) {
    	orientate();
		g.setColor(acolor);
		g.fillPolygon(pxs,pys,5);
		g.setPaintMode();
		g.drawPolygon(pxs,pys,5);
		if (ant.getHasFood()) {
				 g.setColor(Color.green);
				 g.fillOval(foodx,foody,foodw,foodw);
		} 
		g.setColor(Color.black);
    }
    

}
