
package model;

import java.util.Iterator;

public class Cell {

   /** is this cell rocky? Rocky cells cannot contain anything */
   boolean rocky;
     
   /** red anthill? */
   boolean redHill;
   
   /** black anthill? */
   boolean blackHill;
   
   /** food, if any */
   int food;
   
   /** a cell has a set of markers (6 bits) for each color of ant */
   int redMarker = 0;
   
   /** a cell has a set of markers (6 bits) for each color of ant */
   int blackMarker = 0;
   
   /** x-coordinate */
   int x;
   
   /** y-coordinate */
   int y;
   
   /** neighbours, one for each of six directions */
   Cell[] neighbours = new Cell[6];
   
   /** an ant in this cell, if any */
   Ant ant;
   
   public Cell(boolean rocky, boolean redHill, boolean blackHill, int food, int x, int y) {
       super();
       this.rocky = rocky;
       this.redHill = redHill;
       this.blackHill =blackHill;
       this.food = food;
       this.x = x;
       this.y = y;
       this.ant = null;
       if (redHill)
            this.ant = new Ant(Color.RED,this);
       if (blackHill)
            this.ant = new Ant(Color.BLACK,this);
   }
   
   public int getX() {
   		return x;
   }
   
   public int getY() {
   		return y;
   }
   
   /** 
   /** set the given marker bit for the given color */ 
   public void setMarker(int color, int marker) {
       if (color == Color.BLACK)
        	blackMarker |= (1<<marker);
       else
        	redMarker |= (1<<marker);
   }
   
   /** clear all marker bits for the given color */
   public void clearMarker(int color, int marker) {
      if (color == Color.BLACK)
        	blackMarker &= ~(1 << marker);
      else
        	redMarker &= ~(1 << marker);
    }
    
    /** is the given marker bit set for the given color? This method should
     *   only be called by ants of the given color, because ants can only recognise
     *   markers of their own color. */
    public boolean checkMarker(int color,int marker) {
        if (color == Color.BLACK) 
            return (blackMarker & (1 << marker)) != 0;
        else
            return (redMarker & (1 << marker)) != 0;
    }
    
    /** is there any marker at all for the given color? This can be called by ants of
     *   the opposite color, to check whether any marker was deposited by the enemy.
     *   It is not possible, however, to sense what the precise marker was. */
    public boolean checkAnyMarker(int color) {
        if (color == Color.BLACK)
            return blackMarker != 0;
        else
            return redMarker != 0;
    }
      
    /** find the neighbour in the given direction (counter-clockwise, starting with 0 at 4 o'clock) */  
    public Cell getNeighbour(int direction) {
        return neighbours[direction];
    }
    
    /** is there an ant in this cell? */
    public boolean hasAnt() {
        return (ant != null);
    }
    
    /** get the ant, if any */
    public Ant getAnt() {
        return ant;
    }
    
    /** set the ant */
    public void setAnt(Ant ant) {
        this.ant = ant;
    }
    
    /** clear the ant */
    public void clearAnt() {
        ant = null;
    }
    
    /** find the units of food stored here */
    public int getFood() {
        return food;
    }
    
    /** set the units of food stored here */
    public void setFood(int food) {
        this.food = food;
    }
    
    
    /** is this cell part of an anthill of the given color? */
    public boolean isAntHill(int color ) {
        if (color == Color.BLACK)
            return blackHill;
        else
            return redHill;
    }
    
    /** initialise the neighbours array. This method should only be called after all cells have
     *   been created. The neighbours are stored in an array to avoid doing these calculations
     *   repeatedly.
     */
    public void setNeighbours() {
        neighbours[0] = World.v().getCell(x+1,y);
        neighbours[3] = World.v().getCell(x-1,y);
        if (y % 2 == 0) {
            neighbours[1] = World.v().getCell(x,y+1);
            neighbours[2] = World.v().getCell(x-1,y+1);
            neighbours[4] = World.v().getCell(x-1,y-1);
            neighbours[5] = World.v().getCell(x,y-1);
        } else {
            neighbours[1] = World.v().getCell(x+1,y+1);
            neighbours[2] = World.v().getCell(x,y+1);
            neighbours[4] = World.v().getCell(x,y-1);
            neighbours[5] = World.v().getCell(x+1,y-1);
        }
    }
 
   /** is this cell a rock? */
   public boolean getRocky() {
       return rocky;
   }
   
  
   
 
   /** turn a marker into a string */
   private String marksToString(int color) {
   	 String result = "";
   	 for (int i = 0; i<6; i++)
   	 	if (checkMarker(color,i))
   	 		result+=i;
   	  return result;
   }
   
   /** turn a cell into a string */
   public String toString() {
       String result = "cell ("+x+", "+y+"): ";
       if (rocky)
            { result += "rock";
                return result;
            }
	   if (food > 0)
			  result += food + " food; ";
       if (blackHill)
        result += "black hill; ";
       if (redHill)
        result += "red hill; ";
	   if (redMarker != 0)
		  result += "red marks: " + marksToString(Color.RED) + "; ";
       if (blackMarker != 0)
       	result += "black marks: " + marksToString(Color.BLACK) + "; ";
       if (hasAnt())
        result += getAnt().toString();
       return result;
   }
  
   
}
