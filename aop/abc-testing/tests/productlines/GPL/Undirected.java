// Undirected.java   aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import  java.util.LinkedList;

public aspect Undirected 
{
    // ************************************************************
    // Graph class
    public LinkedList Graph.vertices;
    public LinkedList Graph.edges;
    public static final boolean Graph.isDirected = false;
	
    // Class constructor
    public Graph.new() { 
	this.vertices = new LinkedList();
	this.edges = new LinkedList();
    }
   
    // Adds an edge without weights
    public void Graph.addAnEdge(Vertex start, Vertex end, int weight) {
	addEdge(new Edge(start,end));
    }

    // Adds a vertex to the list of vertices
    public void Graph.addVertex(Vertex v ) { 
	vertices.add( v );
    }

    // Adds an edge to the list of edges
    public void Graph.addEdge(Edge the_edge) {
	Vertex start = the_edge.start;
	Vertex end = the_edge.end;
	edges.add(the_edge);
        Neighbor n1 = new Neighbor(end, the_edge);
        Neighbor n2 = new Neighbor(start, the_edge);		 
        start.addNeighbor(n1);  
        end.addNeighbor(n2);         
    }

    // This method adds only the edge and not the neighbor.
    // Used in Transpose layer.
    public void Graph.addOnlyEdge(Edge the_edge) {
	edges.add(the_edge);		 
    }
      


    // Finds a vertex given its name in the vertices list
    public Vertex  Graph.findsVertex(String theName) {
	int i=0;
	Vertex theVertex;
	
	// if we are dealing with the root
	if (theName==null) return null;
	    
	for(i=0; i<vertices.size(); i++)
	{
	    theVertex = (Vertex)vertices.get(i);
	    if (theName.equals(theVertex.name))
		return theVertex;
	}
	return null;
    }

    // Finds an Edge given both of its vertices
    public Edge Graph.findsEdge(Vertex theSource, Vertex theTarget) {
	int i=0;
	Edge theEdge;
	
	for(i=0; i<edges.size(); i++) {
	    theEdge = (Edge)edges.get(i);
	    if ((theEdge.start.name.equals(theSource.name) && 
		 theEdge.end.name.equals(theTarget.name)) ||
		(theEdge.start.name.equals(theTarget.name) && 
		 theEdge.end.name.equals(theSource.name)) )
		return theEdge;
	}   
	return null;	   
    }
	       
    // Displays contents of the graph
    public void Graph.display() {
	int i;
		
	MyLog.println("******************************************");
	MyLog.println("Vertices " + vertices.size());
	for (i=0; i<vertices.size(); i++) 
            ((Vertex) vertices.get(i)).display();
         
	MyLog.println("******************************************");
	MyLog.println("Edges ");
	for (i=0; i<edges.size(); i++)
	    ((Edge) edges.get(i)).display();
		
	MyLog.println("******************************************"); 
    }

    // Method that runs the program
    // Other aspects will attach advice to it to run their corresponding
    // algorithms
    public void Graph.run(Vertex s) { 
	MyLog.println("Running graph algorithms ");
    }

    // ************************************************************
    // Vertex  class
    public LinkedList Vertex.neighbors;
    public String Vertex.name;

    // Constructor
    public Vertex.new(String name) {
       this.name      = name;
       this.neighbors = new LinkedList();
    }
    
    // Adds a neighbor to the vertex 
    public void Vertex.addNeighbor(Neighbor n ) {
	neighbors.add(n);
    }

    // Display the vertex information
    public void Vertex.display() {
	int s = neighbors.size();
	int i;
	
	MyLog.print(" Node " + name + " connected to: ");
	
	for (i=0; i<s; i++) {
	    Neighbor theNeighbor = (Neighbor) neighbors.get(i);
	    MyLog.print( theNeighbor.end.name + ", ");
	}
	MyLog.println();
    }

    // ************************************************************
    // Neighbor class
    public Vertex Neighbor.end;		
    public Edge   Neighbor.edge;
	
    // Default Constructor
    public Neighbor.new() { }

    // Constructor
    public Neighbor.new(Vertex v, Edge e) {
	this.end = v;
	this.edge = e;
    }

    // ************************************************************
    // Edge class
    // The class extension
    declare parents: Edge extends Neighbor;
	
    // Start vertex of an edge
    public Vertex Edge.start;

    // Constructor
    public Edge.new(Vertex the_start, Vertex the_end) {
	start = the_start;
	end = the_end;
    }
		
    // Method to adjust edge adorns
    // Other aspects with attach advice to it 
    public void Edge.adjustAdorns(Edge the_edge) { }		
	
    // Displays the names of the vertices that conform the edge	
    public void Edge.display()  {
	MyLog.println(" start=" + start.name + " end=" + end.name);
    }

}
