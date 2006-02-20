/*
 * Created on 18-Sep-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package command;

import model.Cell;
import model.Ant;

/**
 * @author Oege de Moor
 *
 */
public class Home extends Condition {

      /** is c on the hill of ant a? */
      public boolean matches(Cell c, Ant a) {
          return super.matches(c,a) &&
                      c.isAntHill(a.getColor());
      }
  
    public String toString() {
          return "Home";
    }
}
