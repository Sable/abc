// CC.java ConnectedComponents   aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import  java.util.LinkedList;

public aspect CC 
{
    // *****************************************************************
    // **** Graph class
    // Executes Strongly Connected Components
    // A point cut to calls to Graph run
    // Gets the pointcuts of the target that call run 
    pointcut graph_run(Graph g, Vertex v): target(g) && args(v) && 
	call(void Graph.run(Vertex));
    
    // An after advice to run Vertex Numbering 
    after(Graph g, Vertex v): graph_run(g,v) {
	MyLog.println("Running Connected Components ");
	g.ConnectedComponents(); 	
    }

    // Runs the connected components
    public void Graph.ConnectedComponents() {
	GraphSearch( new RegionWorkSpace());
    }
    
    // *****************************************************************
    // **** Vertex class
    public int Vertex.componentNumber;

    // A point cut to calls to Vertex.display, originally overriden
    // Gets the pointcuts of the targets that call display of a Vertex 
    pointcut vertex_display(Vertex v): target(v) && 
	call(void Vertex.display());
    
    // A before advice to display the vertex number 
    before(Vertex v): vertex_display(v) {
	MyLog.print(" comp# "+ v.componentNumber + " ");
    }

    // *****************************************************************
    // **** RegionWorkspace class extends the WorkSpace class
    declare parents: RegionWorkSpace extends WorkSpace;

    public int RegionWorkSpace.counter;

    public RegionWorkSpace.new() {
	    counter = 0;
    }

    public void RegionWorkSpace.init_vertex(Vertex v ) {
	v.componentNumber = -1;
    }
	  
    public void RegionWorkSpace.postVisitAction(Vertex v ) {
	v.componentNumber = counter;
    }

    public void RegionWorkSpace.nextRegionAction(Vertex v ) {
	counter ++;
    }

} // Connected Components aspect
