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
public class FoeMarker extends Condition {
    
      /** has any given marker been set at cell a for foes of a? */
      public boolean matches(Cell c, Ant a) {
          if (!super.matches(c,a))
            return false;
          if (a.getColor() == Color.BLACK)
              return c.checkAnyMarker(Color.RED);
          else
              return c.checkAnyMarker(Color.BLACK);
      }
    
    public String toString() {
          return "FoeMarker";
      }
}
