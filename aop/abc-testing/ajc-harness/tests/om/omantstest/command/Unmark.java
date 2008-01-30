
package command;

import model.Ant;

public class Unmark implements Command {
    int marker;
      int state;

   
  public void step(Ant a) {
      a.getPosition().clearMarker(a.getColor(),marker);
      a.setState(state);
  }
    
  public Unmark(int marker, int state) {
      this.marker = marker;
      this.state = state;
  }
  
  public String toString() {
        return "Unmark "+marker+ " " + state;
  }
  
}
