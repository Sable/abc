
package model;

public class Ant {
	
	static int idCount = 0;
	
	/** identity of ant for printing */
	protected int identity = 0;

    /** state of ant automaton */    
    protected int state = 0;  
    
    /** color of this ant (red or black) */   
    protected int color;      
    
    /** which way the ant is facing (0..5) */            
    protected int direction = 0;   
    
    /** whether the ant is carrying food */      
    protected boolean hasFood = false; 
    
    /** where the ant is at */
    protected Cell position;
    
  
    /** create a new ant */
    public Ant(int color, Cell position) {
        super();
        this.color = color;
        this.position = position;
        this.identity = idCount++;
    }
    
    /** return the state of this ant */
    public int getState() {
        return state;
    }
    
    /** set the state of this ant */
    public void setState(int state) {
        this.state = state;
    }
    
    /** what color is this ant? */
    public int getColor() {
        return color;
    }
    
    /** set color of this ant */
    public void setColor(int color) {
        this.color = color;
    }
    
    /** what way is the ant facing (0..5) ? */
    public int getDirection() {
        return direction;
    }
    
    /** is this ant carrying food? */
    public boolean getHasFood() {
        return hasFood;
    }
    
    /** set the food carrying flag */
    public void setHasFood(boolean hasFood) {
        this.hasFood = hasFood;
    }
    
    /** where is this ant? */
    public Cell getPosition() {
        return position;
    }
    
    /** place this ant */
    public void setPosition(Cell pos) {
        position.clearAnt();
        position = pos;
        position.setAnt(this);
    }

    /** give the new direction upon a left turn (but don't execute the turn) */
    private int leftturn() {
        return (direction + 5) % 6;
    }
    
    /** give the new direction upon a right turn (but don't execute the turn) */
    private int rightturn() {
        return (direction + 1) % 6;
    }
    
    /** turn to the left */
     public void turnLeft(){
         direction = leftturn();
     }
     
     /** turn to the right */
     public void turnRight() {
         direction = rightturn();
     }
     
     /** a class to encode the different directions to sense in */
     public static class SenseDir {
     	/** sensing the current cell */
         public static final int HERE = 0;
         /** sensing straight ahead (in the direction the ant is facing) */
         public static final int AHEAD = 1;
         /** sensing ahead, but to the left of the direction the ant is facing */
         public static final int LEFTAHEAD = 2;
         /** sensing ahead, but to the right of the direction the ant is facing */
         public static final int RIGHTAHEAD = 3;
         
         /** convert a sense direction into a string */
         public static String toString(int senseDir) {
             switch (senseDir) {
                 case HERE : return "Here"; 
                 case AHEAD : return "Ahead";
                 case LEFTAHEAD : return "LeftAhead";
                 case RIGHTAHEAD : return "RightAhead";
                 default : throw new RuntimeException("Invalid senseDir");
             }
         }
     }
     
     /** return the adjacent cell according to the given sense direction */
     public Cell senseNeighbour(int sensedir) {
         switch (sensedir) {
            case SenseDir.HERE : return getPosition();
            case SenseDir.AHEAD : return getPosition().getNeighbour(direction);
            case SenseDir.LEFTAHEAD : return getPosition().getNeighbour(leftturn());
            case SenseDir.RIGHTAHEAD : return getPosition().getNeighbour(rightturn());
            default: return null;
         } 
     }
    
    /** string representation of ant */
   	public String toString() {
   		return Color.toString(getColor()) + " ant of id "+identity+", dir "+direction+", food "+ (hasFood? 1:0)+ ", state "+state;
   }
   	
}
