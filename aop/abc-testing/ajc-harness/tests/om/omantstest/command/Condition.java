/*
 * Created on 18-Sep-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package command;

import model.Ant;
import model.Cell;

/**
 * @author Oege de Moor
 *
 */
public abstract class Condition {
    
    /** does this condition match at cell c for ant a? */
    public boolean matches(Cell c, Ant a) {
        return !c.getRocky();
    }
    
    public abstract String toString();

}
