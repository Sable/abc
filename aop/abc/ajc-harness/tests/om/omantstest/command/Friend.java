/*
 * Created on 18-Sep-2004
 *
 */
package command;

import model.Ant;
import model.Cell;

public class Friend extends Condition {

    /** is there a friend of a at cell c? */
    public boolean matches(Cell c, Ant a) {
        return super.matches(c,a) &&
                   c.hasAnt() &&
                   (c.getAnt().getColor() == a.getColor());
    }
    
    public String toString() {
        return "Friend";
    }

}
