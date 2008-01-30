/*
 * Created on 18-Sep-2004
 *
 */
package command;

import model.Cell;
import model.Ant;
import model.Color;

/**
 * @author Oege de Moor
 *
 */
public class FoeHome extends Condition {
    /** is c on the foe hill of ant a? */
     public boolean matches(Cell c, Ant a) {
         if (!super.matches(c,a))
            return false;
         if (a.getColor() == Color.BLACK)
            return c.isAntHill(Color.RED);
         else
            return c.isAntHill(Color.BLACK);
     }
  
    public String toString() {
          return "FoeHome";
      }
}
