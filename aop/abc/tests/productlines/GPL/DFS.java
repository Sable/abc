// DFS.java   aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import  java.util.LinkedList;

public aspect DFS 
{
    // *********************************************************************
    // **** Graph class
    // Graph search receives a Working Space 
    public void Graph.GraphSearch(WorkSpace w) {
	int           s, c;
	Vertex  v;
  
	// Step 1: initialize visited member of all nodes

	s = vertices.size();
	if (s == 0) return;             // if there are no vertices return
         
	// Initializig the vertices
	for (c = 0; c < s; c++) {
	    v = (Vertex) vertices.get(c);  
	    v.init_vertex( w );
	}

	// Step 2: traverse neighbors of each node
	for (c = 0; c < s; c++) {
	    v = (Vertex) vertices.get(c);  
	    if (!v.visited)  {
		w.nextRegionAction(v);
		v.dftNodeSearch( w);
	    }
	} //end for
    } // end of GraphSearch
   
    // ****************** Order Testing
    pointcut graph_order(Graph g): target(g) && call(void Graph.order());

    // An before advice to display the weigth of the edge
    void around(Graph g): graph_order(g) {
	MyLog.println("DFS Aspect ");
	proceed(g);
    }

    // ********************************************************************
    // **** Vertex class
    public boolean Vertex.visited;
   
    public Vertex.new() {
	    visited = false;
    }
      
    public void Vertex.init_vertex( WorkSpace w ) {
         visited = false;
         w.init_vertex((Vertex) this);
    } 
   
    public void Vertex.dftNodeSearch( WorkSpace w) {
         int           s, c;
         Vertex v;
         Neighbor n;

         // Step 1: Do preVisitAction. 
	 //	    If we've already visited this node return

         w.preVisitAction((Vertex) this);
         
         if (visited) return;

         // Step 2: else remember that we've visited and 
         //         visit all neighbors

         visited = true;
         
	 s = neighbors.size();
	 for (c = 0; c < s; c++) {
	     n = (Neighbor) neighbors.get(c);
	     v = n.end;
	     w.checkNeighborAction((Vertex) this, v);  
	     v.dftNodeSearch( w);
         };
     
         // Step 3: do postVisitAction now
         w.postVisitAction((Vertex) this);
	 
    } // of dftNodeSearch

    // A point cut to calls to Vertex.display
    // Gets the pointcuts of the targets that call display of a Vertex 
    pointcut vertex_display(Vertex v): target(v) && 
	call(void Vertex.display());

    // A before advice to display if the vertex has been visited or not
    before(Vertex v): vertex_display(v) {
	if (v.visited)
            MyLog.println("  visited ");
	else
            MyLog.println(" !visited ");
    }

} // end of aspect DFS
