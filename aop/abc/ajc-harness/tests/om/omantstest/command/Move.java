
package command;

import model.Ant;

import model.Cell;

public class Move implements Command {

    int cannotMove;
    int doMove;
    
    public Move(int doMove, int cannotMove) {
        this.doMove = doMove;
        this.cannotMove = cannotMove;
    }
    
    /** move forward if possible */
    public void step(Ant a) {
        Cell newPos = a.getPosition().getNeighbour(a.getDirection());
        if (newPos.getRocky() || newPos.hasAnt()) {
            a.setState(cannotMove);
            return;
        }
        a.setPosition(newPos);
        a.setState(doMove);
    }

    public String toString() {
          return "Move "+doMove+" "+cannotMove;
    }
}
