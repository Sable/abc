/*
 * Created on 18-Sep-2004
 *
 */
 
package command;
import model.Cell;
import model.Ant;
/**
 * @author Oege de Moor
 *
 */
public class Food extends Condition {
    
    /** is there food at cell c? */
    public boolean matches(Cell c, Ant a) {
        return super.matches(c,a) && 
                   c.getFood() > 0;
    }
    
   
    public String toString() {
        return "Food";
    }

    
}
