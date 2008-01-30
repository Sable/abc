
package command;

import model.Ant;

public class PickUp implements Command {

    protected int hasFoodAlready;
    protected int newFood;
    
    /** pick up food, if possible */
    public void step(Ant a) {
        if (a.getHasFood() || a.getPosition().getFood() == 0)
            a.setState(hasFoodAlready);
        else {
            a.getPosition().setFood(a.getPosition().getFood() -1);
            a.setHasFood(true);
            a.setState(newFood);
        }
    }
    
    public PickUp(int newFood, int hasFoodAlready) {
        this.newFood = newFood;
        this.hasFoodAlready = hasFoodAlready;
    }
    
    public String toString() {
        return "PickUp "+ newFood + " " +hasFoodAlready;
    }
    
}
