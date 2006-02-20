
package command;

import model.Ant;
import model.Cell;

public class Sense implements Command {

    /** the direction to sense in.
     *   @see model.Ant.SenseDir;
     */
    int senseDir;
    
    int thenBranch;
    int elseBranch;
    
    Condition condition;
    
    public Sense(int senseDir, int thenBranch, int elseBranch, Condition condition) {
        this.senseDir = senseDir;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
        this.condition = condition;
    }
    
    public void step(Ant ant) {
        Cell sensedPosition = ant.senseNeighbour(senseDir);
        if (condition.matches(sensedPosition,ant))
            ant.setState(thenBranch);
        else
            ant.setState(elseBranch);
    }
    
    public String toString() {
        return "Sense "+ Ant.SenseDir.toString(senseDir)+ "  " + thenBranch + " " + elseBranch;
    }
   

}
