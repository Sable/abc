
package command;

import model.Ant;

public interface Command {
    
    void step(Ant a);
    
    String toString();
    
}
