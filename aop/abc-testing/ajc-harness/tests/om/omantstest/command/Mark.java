package command;

import model.Ant;

public class Mark implements Command {
    
        int marker;
        int state;
   
    /** place a marker and change state */
    public void step(Ant a) {
        a.getPosition().setMarker(a.getColor(),marker);
        a.setState(state);
    }
    
    public Mark(int marker, int state) {
        this.marker = marker;
        this.state = state;
    }
    
    public String toString() {
          return "Mark "+marker+" "+state;
    }

}
