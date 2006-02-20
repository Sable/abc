
package viewer;

import model.Ant;
import model.Cell;
import model.World;
import command.*;

public aspect Update {

	Viewer v;
	
	void register(Viewer v) {
		this.v = v;
	}

	/** when to update a single hexagon */
	after(Ant a) : (call (void Command.step(..)) &&
							  (target(Drop) || target(Turn) || target(PickUp)) && args(a))  
							  || (call(void Ant.kill(..)) && target (a)) {
			 v.update(a.getPosition()); 
	}
	

	/** for a move, we have to update both the old and the new position */
	void around(Ant a) : call (void Command.step(..)) && target(Move) && args(a)
	{
		Cell oldPos = a.getPosition();
		proceed(a);
		if (a.getPosition() != oldPos) {
			v.update(oldPos);
		    v.update(a.getPosition());
		}
	}
	
	/** slowing down the simulation */
	before() : call (void Command.step(..))
	{
		for (int i = 0; i< v.slowDown*100; i++);
	}

	/** keeping track of the round in the visualiser */
	before() : call (void World.round(..))
	{
		v.incRound();
	}
	
	
	/** keeping the score */
	void around(Ant a) :    (call (void Command.step(..)) && (target(PickUp) || target(Drop)) && args(a))
								     || (call (void Ant.kill(..)) && target(a))
	{
			int oldFood = a.getPosition().getFood();
			proceed(a);
			updateFood(a,oldFood);
	} 
	
	void updateFood(Ant a, int oldFood)
		{
			int diff = a.getPosition().getFood() - oldFood;
			if (diff == 0)
				return;
			if (a.getPosition().isAntHill(model.Color.BLACK))
				v.upBlack(diff);
			if (a.getPosition().isAntHill(model.Color.RED))
				v.upRed(diff); 
		}
	
	
	
}
