// Weighted.java   aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import  java.util.LinkedList;

public aspect Weighted
{
    // *********************************************************************
    // **** Graph
    // Adds an edge with weights 
    // Gets the joint-points of the targets that call addAnEdge in Graph
    pointcut graph_addAnEdge(Graph g, Vertex start, Vertex end, int weigth): 
	target(g) && args(start,end,weigth) && 
	call(void Graph.addAnEdge(Vertex, Vertex, int));

    // An around advice to add the weigth of the edge
    void around(Graph g, Vertex start, Vertex end, int weigth): 
	graph_addAnEdge(g, start, end, weigth) {
	Edge e = new Edge(start, end, weigth);
	g.addEdge(e);   

	// Around allows us to avoid proceeding with the addAnEdge method
        // Defined in Directed or Undirected which is what we want since we
	// have already created the Edge object with its weight
	// The syntax of proceed would be 
	// proceed(g,start,end,weigth);
    }
    
    // *********************************************************************
    // **** Edge 
    public int Edge.weight;

    // Constructor with weight added
    public Edge.new(Vertex the_start, Vertex the_end, int the_weight)
    {
     	this.start = the_start;
	this.end = the_end;
	this.weight = the_weight;
    }			
    	
    // A point cut to calls to adjustAdorns
    // Gets the pointcuts of the targets that call adjustAdorns in Edge
    pointcut edge_adjustAdorns(Edge e, Edge other_edge): 
	target(e) && args(other_edge) && 
	call(void Edge.adjustAdorns(Edge));

    // An around advice to display the weigth of the edge
    void around(Edge e, Edge other_edge): 
	edge_adjustAdorns(e, other_edge) {
	e.weight = other_edge.weight;
	
	// Continues with the next adjustAdorn method down
	proceed(e,other_edge);
        
	// quasi-equivalent to super.adjustAdorns(the_edge);
    }
  
    // A point cut to calls to Edge.display, originally overriden
    // Gets the pointcuts of the targets that call display of an Edge
    pointcut edge_display(Edge e): target(e) && call(void Edge.display());

    // A before advice to display the weigth of the edge
    before(Edge e): edge_display(e) {
	MyLog.print("Weight = " + e.weight);
    }

}  // end of aspect
