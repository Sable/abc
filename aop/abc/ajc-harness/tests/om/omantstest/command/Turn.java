
package command;

import model.Ant;

public class Turn implements Command {

    boolean left;
    int state;
 
    public Turn(boolean left, int state) {
        this.left = left;
        this.state = state;
    }
    
    public void step(Ant a) {
        if (left)
            a.turnLeft();
        else
            a.turnRight();
        a.setState(state);
    }
    
    public String toString() {
          return "Turn " + (left ? "Left " : "Right ") + state;
    }
}
