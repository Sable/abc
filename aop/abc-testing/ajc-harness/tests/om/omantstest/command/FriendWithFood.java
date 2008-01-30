/*
 * Created on 18-Sep-2004
 *
 */
package command;

import model.Cell;
import model.Ant;

public class FriendWithFood extends Friend {

    /** is there a friend of a at cell c that is carrying food? */
     public boolean matches(Cell c, Ant a) {
         return super.matches(c,a) &&
                    c.getAnt().getHasFood();
     }
     
   
    public String toString() {
        return "FriendWithFood";
    }

}
