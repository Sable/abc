/*
 * Created on 18-Sep-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package model;

/**
 * @author Oege de Moor
 *
 */
public class Color {

    public static final int BLACK = 0;
    public static final int RED = 1;
    /**
     * 
     */
    public Color() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public static String toString(int color) {
    	if (color == BLACK)
    		return "black";
    	else
    		return "red";
    }

}
