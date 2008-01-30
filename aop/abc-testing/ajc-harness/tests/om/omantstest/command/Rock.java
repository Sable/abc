
package command;

import model.Cell;
import model.Ant;

public class Rock extends Condition {

    public boolean matches(Cell c, Ant a) {
        return c.getRocky();
    }
    
    public String toString() {
        return "Rock";
    }
}
