
package model;
import java.util.Iterator;

/** aspect that encodes the martial arts of ants */
aspect Combat {

    /** is this ant next to any enemies? */
	private int Ant.adjacentFoes() {
		int result = 0;
		for (int i = 0; i<6; i++){
			Cell neighbour = getPosition().neighbours[i];
			if (neighbour.hasAnt() &&
				neighbour.getAnt().getColor() != getColor())
				result++;
		}
		return result;
	}
	
	/** kill this ant */
	private void Ant.kill() {
		  position.setFood(position.getFood() + 3 + (hasFood ? 1 : 0));
		  position.clearAnt();
		  dead = true;
	}
    
	/** check whether this cell contains a surrounded ant, and if it does, kill it */
	private void Cell.checkForSurroundedAnt() {
		 if (hasAnt()) 
			 if (ant.adjacentFoes() >= 5) 
				 ant.kill();
	 }
   
	 /** check whether this cell or any of its neighbours contain a surrounded ant */
	 private void Cell.checkForSurroundedAnts() {
		 checkForSurroundedAnt();
		 for (int i = 0; i< 6; i++){
			 neighbours[i].checkForSurroundedAnt();
		 }
	 }
	 
	 /** make check for ants to be killed after every move */
	 after(Cell c) : call(void Ant.setPosition(Cell)) && args(c){
	 	c.checkForSurroundedAnts();
	 }
	 
	 /** has this ant been killed? */
	 private boolean Ant.dead = false;
    
	 /** has this ant been killed? - used to keep the list of live ants in World */
	 boolean Ant.getDead() {
		return dead;
	 }
}
