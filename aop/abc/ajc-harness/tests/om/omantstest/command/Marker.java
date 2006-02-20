
package command;

import model.Cell;
import model.Ant;


public class Marker extends Condition {
    
    int marker;
    
    /** has the given marker been set at cell a for the colour of a? */
    public boolean matches(Cell c, Ant a) {
        return super.matches(c,a) &&
                    c.checkMarker(a.getColor(),marker);
    }
    
    public Marker(int marker) {
        super();
        this.marker = marker;
    }
    
   
    public String toString() {
        return "Marker";
    }

    
}
