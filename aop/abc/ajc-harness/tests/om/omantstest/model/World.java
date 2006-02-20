
package model;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Random;
import java.io.*;
import automaton.Automaton;

public class World {
	
	/** there is at most one world */
	private static World singleton;
    
    /** grid of cells */
    private Cell[][] grid;
	/** x coordinates are in interval [0,maxX) */
	private int maxX = 100;
	/** y coordinates are in interval [0,maxY) */ 
	private int maxY = 100;
    /** behaviour of red ants */  
    private Automaton redAnt;
    /** behaviour of black ants */
    private Automaton blackAnt;
   
    /** file for resetting the map of the world */
    protected File worldFile;
        
    /** seed for random number generator */
    private final static int seed = 12345;
    /** random number generator */
    private Random random = new Random(seed);
    
    /** how many rounds we shall play */
    private static int numberOfRounds = 100000;
   
    /** the list of live ants */
    List ants;
    
    public int getMaxX() {
    	return maxX;
    }
    
    public int getMaxY() {
    	return maxY;
    }

    public int getSeed() {
	return seed;
    }
    
    public Random getRandom() {
    	return random;
    }
    
    /** read in and parse a world from the given source */
    public void readWorld(DataInput in) throws IOException {
        String maxXstr = in.readLine();
        maxX = (new Integer(maxXstr)).intValue();
        String maxYstr = in.readLine();
        maxY = (new Integer(maxYstr)).intValue();
        grid = new Cell[maxX][maxY];
        ants = new LinkedList();
        for (int y = 0; y < maxY; y++) {
            String line = in.readLine();
            char[] linearr = line.toCharArray(); 
            int j = 0;
            for (int x = 0; x < maxX; x++)  {
                while (Character.isWhitespace(linearr[j])) j++;
                char curChar = linearr[j++];
                switch (curChar) {
                    case '#' : {grid[x][y] = new Cell(true,false,false,0,x,y); break; }
                    case '.' : { grid[x][y] = new Cell(false,false,false,0,x,y); break; }
                    case '-' :  {   grid[x][y] = new Cell(false,false,true,0,x,y);
                                        ants.add(grid[x][y].getAnt());
                                        break;
                                    }
                    case '+' : {  grid[x][y] = new Cell(false,true,false,0,x,y);
                                        ants.add(grid[x][y].getAnt());
                                        break;
                                    }
                    default : 
                                   if (Character.isDigit(curChar)) {
                                       Character charObj = new Character(curChar);
                                       int food = (new Integer(charObj.toString())).intValue();
                                       grid[x][y] = new Cell(false,false,false,food,x,y);
                                   } else throw new RuntimeException ("error reading map at line "+y);
                }
            }
        }
        for (int y=0; y < maxY; y++)
            for (int x=0; x < maxX; x++)
                grid[x][y].setNeighbours();
    }
    
    /** retrieve a cell from the grid */
    public Cell getCell(int x, int y) {
        if (x <0 || y <0 || x >= maxX || y >= maxY)
            return null;
        return grid[x][y];
    }
   
    /** return the singleton instance of World */
    public static World v() {
        return singleton;
    }
    
    /** do one step on all the live ants */
    public void round() {
		   Iterator current = ants.iterator();
		   while (current.hasNext()) {
			   Ant ant = (Ant) current.next();
			   if (ant.getDead())
				   current.remove();
			   else if (ant.getColor() == Color.BLACK)
						   blackAnt.step(ant);
					   else
						   redAnt.step(ant);
		   }
	}
   
    /** do a simulation */
    public void play() {
         for (int i = 1; i <= numberOfRounds; i++)
            round();	
    }
    
 
    /** create a blank world */
    public World() {
        super();
        singleton = this;
		grid = new Cell[maxX][maxY];
		ants = new LinkedList();
		for (int y = 0; y < maxY; y++)
			for (int x = 0; x < maxX; x++)
				grid[x][y] = new Cell(false,false,false,0,x,y);
		for (int y=0; y < maxY; y++)
		   for (int x=0; x < maxX; x++)
			   grid[x][y].setNeighbours();
    }
    
    /** initialise an ant of the given color from the given file */
    public void loadAnt(int color, File f) throws IOException {
    	if (color == Color.BLACK)
    		blackAnt = new Automaton(new DataInputStream(new FileInputStream(f))); 
    	else
    		redAnt = new Automaton(new DataInputStream(new FileInputStream(f)));
    }
    
    /** load a new map from the given file */
    public void loadWorld(File f) throws IOException {
    	worldFile = f; readWorld(new DataInputStream(new FileInputStream(f)));
    }
    
    /** go back to the original state (for replay with the same world) */
    public void reset() throws IOException {
		random = new Random(seed);
    	readWorld(new DataInputStream(new FileInputStream(worldFile))); 	
    }
    
    
    
    

}
