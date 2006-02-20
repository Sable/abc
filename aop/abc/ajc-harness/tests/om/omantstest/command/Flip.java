package command;

import model.Ant;
import model.World;

public class Flip implements Command {

    int n;
    int thenBranch;
    int elseBranch;
    
    public Flip(int n, int thenBranch, int elseBranch) {
        this.n = n;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    /** roll dice and branch to a new state depending on outcome */
    public void step(Ant a) {
        if (World.v().getRandom().nextInt(n) == 0)
            a.setState(thenBranch);
       else
            a.setState(elseBranch);
    }

   
    public String toString() {
        return "Flip "+n + " " + thenBranch + " " + elseBranch;
    }

}
