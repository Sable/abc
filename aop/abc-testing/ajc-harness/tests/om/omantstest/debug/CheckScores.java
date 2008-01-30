
package debug;

import model.World;
import model.Ant;
import command.Command;
import viewer.Viewer;
import viewer.Update;

/** aspect to check that the incrementally computed scores are correct */
privileged aspect CheckScores {
	
	/** make sure that updates get done before we check their result */
	declare precedence : CheckScores,Update;
	
	/** a naive way of computing the score */
	public int World.foodOnHill(int color) {
		 int result= 0;
		 for (int x = 0; x < maxX; x++)
			 for (int y=0; y < maxY; y++) {
				if (grid[x][y].isAntHill(color))
					result+= grid[x][y].getFood();
		 }
		 return result;
	 }
	 
	 /** check the score after every step */
	after(Ant a,Command c) : call (void Command.step(..)) && args(a) && target(c) {
			Viewer v = Update.aspectOf().v;
			if (v.blackFood != v.world.foodOnHill(model.Color.BLACK))
				{System.out.println("black food discrepancy after "+c); 
					System.out.println("v.blackFood="+v.blackFood);
					System.out.println("v.world.foodOnHill(model.Color.BLACK)="+
												 v.world.foodOnHill(model.Color.BLACK));
					System.exit(1);} 
			if (v.redFood != v.world.foodOnHill(model.Color.RED))
				{System.out.println("red food discrepancy after "+c); 
					System.out.println("v.redFood="+v.redFood);
					System.out.println("v.world.foodOnHill(model.Color.RED)="+
												 v.world.foodOnHill(model.Color.RED));
					System.exit(1);} 
    
		} 
}
