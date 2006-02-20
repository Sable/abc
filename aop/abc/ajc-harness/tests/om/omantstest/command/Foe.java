/*
 * Created on 18-Sep-2004
 *
 */
 
package command;

import model.Cell;
import model.Ant;


public class Foe extends Condition {
 
      /** is there a foe of a at cell c? */
      public boolean matches(Cell c, Ant a) {
          return super.matches(c,a) &&
                     c.hasAnt() &&
                     (c.getAnt().getColor() != a.getColor());
      }
      
   
    public  String toString() {
        return "Foe";
    }

}
