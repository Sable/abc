// Number.java   aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import  java.util.LinkedList;

public aspect Cycle
{
   // ******************************************************************
   // **** Graph class 
   // Executes Cycle Checking
   // A point cut to calls to Vertex.display
   // Gets the pointcuts of the targets that call display of a Vertex 
    pointcut graph_run(Graph g, Vertex v): target(g) && args(v) && 
	call(void Graph.run(Vertex));

    // A before advice to display the vertex number 
    after(Graph g, Vertex v): graph_run(g,v) {
	System.out.println("Running Cycle Checking");
	System.out.println (" Cycle? " + g.CycleCheck());
    }
	
   // Method that call the cycle checking algorithm 
   public boolean Graph.CycleCheck() {
       CycleWorkSpace c = new CycleWorkSpace(isDirected);
       GraphSearch( c );
       return c.AnyCycles;
   }

   // *************************************************************************
   // **** Class Vertex 
   public int Vertex.VertexCycle;      
   public int Vertex.VertexColor; // white ->0, gray ->1, black->2

    // A point cut to calls to Vertex.display, originally overriden
    // Gets the pointcuts of the targets that call display of a Vertex 
    pointcut vertex_display(Vertex v): target(v) && 
	call(void Vertex.display());

    // A before advice to display the vertex number 
    before(Vertex v): vertex_display(v) {
      MyLog.print(" VertexCycle# " + v.VertexCycle + " ");
    }
	
   // ************************************************************************
   // *** CycleWorkSpace
   declare parents: CycleWorkSpace extends WorkSpace;

   // Auxiliary variables
   public boolean CycleWorkSpace.AnyCycles;
   public int     CycleWorkSpace.counter;
   public boolean CycleWorkSpace.isDirected;
	  
   // Colors to distinguish the status of the search
   public static final int CycleWorkSpace.WHITE = 0;
   public static final int CycleWorkSpace.GRAY  = 1;
   public static final int CycleWorkSpace.BLACK = 2;	  
	        
   // Constructor
   public CycleWorkSpace.new(boolean UnDir) {
         AnyCycles = false;
         counter   = 0;
         isDirected = UnDir;
   }

   // To initialize the vetices 
   public void CycleWorkSpace.init_vertex(Vertex v ) {	
       v.VertexCycle = Integer.MAX_VALUE;
       v.VertexColor = WHITE;		// initialize to white color
   }

   public void CycleWorkSpace.preVisitAction(Vertex v ) { 	
       // This assigns the values on the way in
       if (v.visited!=true) 
	   {  // if it has not been visited then set the
	       // VertexCycle accordingly
	       v.VertexCycle = counter++;
	       v.VertexColor = GRAY; // we make the vertex gray
	   }
   }

   public void CycleWorkSpace.postVisitAction(Vertex v ){ 
       v.VertexColor = BLACK;  // we are done visiting, make it black
       counter--;
   } // of postVisitAction


   public void CycleWorkSpace.checkNeighborAction(Vertex vsource, 
						  Vertex vtarget) { 
       // if the graph is directed is enough to check that the source node
       // is gray and the adyacent is gray also to find a cycle
       // if the graph is undirected we need to check that the adyacent is not
       // the father, if it is the father the difference in the VertexCount is
       // only one.				   
       if (isDirected) {
	   if ((vsource.VertexColor == GRAY ) && 
	       (vtarget.VertexColor == GRAY)) {
	       AnyCycles = true;		
	   }
       }
       else
	   {  // undirected case
	       if ((vsource.VertexColor == GRAY ) && 
		   (vtarget.VertexColor == GRAY) &&
		   vsource.VertexCycle != vtarget.VertexCycle+1) {
		   AnyCycles = true;		
	       }
	   }     
   } // of checkNeighboor

} // end of Cycle

