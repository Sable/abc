// MSTKruskal.java   aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import java.util.LinkedList;
import java.lang.Integer;
import java.util.Collections;
import java.util.Comparator;

public aspect MSTKruskal 
{
    // ******************************************************************
    // **** Graph class
    // Executes Strongly Connected Components
    // A point cut to calls to Graph run, originally overriden
    // Gets the pointcuts of the target that call run 
    pointcut graph_run(Graph g, Vertex v): target(g) && args(v) && 
	call(void Graph.run(Vertex));
    
    // An after advice to run Vertex Numbering 
    after(Graph g, Vertex v): graph_run(g,v) {
	System.out.println("Running MST Kruskal ");
	Graph gaux = g.Kruskal();
	g.stopProfile();
	if (MyLog.dumpgraph) gaux.display(); 
	g.resumeProfile(); 
    }

    // Computes the strongly connected components
    public Graph Graph.Kruskal() {      
	// 1. A <- Empty set
	LinkedList A = new LinkedList();
		
	// 2. for each vertex v E V[G]
	// 3.    do Make-Set(v)
	int numvertices = vertices.size();
	int i;
	Vertex v;
		
	for (i=0; i < numvertices; i++)
	{
	    v = (Vertex)vertices.get(i);
	    v.representative = v;		// I am in my set
	    v.members = new LinkedList();	// I have no members in my set
	}
		
	// 4. sort the edges of E by nondecreasing weight w	
	// Creates the edges objects
	int j;
	LinkedList Vneighbors = new LinkedList();
	Vertex u;
		
	// Sort the Edges in non decreasing order
        Collections.sort(edges, 
	  new Comparator()
	   {
	     public int compare (Object o1, Object o2)
	     { 
		Edge e1 = (Edge)o1;
		Edge e2 = (Edge)o2;
	        if (e1.weight < e2.weight)
		  return -1;
		if (e1.weight == e2.weight)
  		  return 0;
		return 1;	   		 
	     }
	  });		
        
	// 5. for each edge in the nondecresing order
	int numedges = edges.size();
	Edge e1;
	Vertex vaux, urep, vrep;
		
	for(i=0; i<numedges; i++) {
	    // 6. if Find-Set(u)!=Find-Set(v)
	    e1 = (Edge)edges.get(i);
	    u = e1.start;
	    v = e1.end;
	    
	    if (!(v.representative.name).equals(u.representative.name)) {
		// 7. A <- A U {(u,v)}
		A.add(e1);
		
		// 8. Union(u,v)
		urep = u.representative;
		vrep = v.representative;
		
		if ((urep.members).size() > (vrep.members).size())
		    { // we add elements of v to u
			for(j=0; j<(vrep.members).size(); j++) {
			    vaux = (Vertex)(vrep.members).get(j);
			    vaux.representative = urep;
			    (urep.members).add(vaux);
			}
			v.representative = urep;
			vrep.representative = urep;
			(urep.members).add(v);
			if (!v.equals(vrep)) (urep.members).add(vrep);
			(vrep.members).clear();
		    }
	     else
	     { // we add elements of u to v
		 for(j=0; j<(urep.members).size(); j++) {
		 vaux = (Vertex)(urep.members).get(j);
		 vaux.representative = vrep;
		 (vrep.members).add(vaux);
		 }
	       u.representative = vrep;
	       urep.representative = vrep;
	       (vrep.members).add(u);
	       if (!u.equals(urep)) (vrep.members).add(urep);
	       (urep.members).clear();
	       
	     } // else
		
	    } // of if
	    
	} // of for numedges
		
	// 9. return A
      // Creates the new Graph that contains the SSSP
	String theName;
	Graph newGraph = new Graph();
		
	// Creates and adds the vertices with the same name
	for (i=0; i<numvertices; i++) {
	    theName = ((Vertex)vertices.get(i)).name;		
	    newGraph.addVertex(new Vertex(theName));
	}
        
	// Creates the edges from the NewGraph
	Vertex theStart, theEnd;
	Vertex theNewStart, theNewEnd;
	Edge   theEdge;
       
	// For each edge in A we find its two vertices
	// make an edge for the new graph from with the correspoding
	// new two vertices
	for(i=0; i<A.size(); i++) {   
	    // theEdge with its two vertices
	    theEdge = (Edge)A.get(i);
	    theStart = theEdge.start;
	    theEnd = theEdge.end;
	    
	    // Find the references in the new Graph
	    theNewStart = newGraph.findsVertex(theStart.name);
	    theNewEnd = newGraph.findsVertex(theEnd.name);
         
	    // Creates the new edge with new start and end vertices 
	    //in the newGraph
	    // and ajusts the adorns based on the old edge
	    Edge theNewEdge = new Edge(theNewStart, theNewEnd);
	    theNewEdge.adjustAdorns(theEdge);
	    
	    // Adds the new edge to the newGraph
	    newGraph.addEdge(theNewEdge);
	}          
        return newGraph;	
        
    } // of Kruskal

    // ***************************************************************	
    // **** Vertex class
    public Vertex Vertex.representative;
    public LinkedList Vertex.members;

    // A point cut to calls to Vertex.display
    // Gets the pointcuts of the targets that call display of a Vertex 
    pointcut vertex_display(Vertex v): target(v) && 
	call(void Vertex.display());

    // A before advice to display the vertex number 
    before(Vertex v): vertex_display(v) {
	if (v.representative == null)
           MyLog.print("Rep null "); 
	else	
	    MyLog.print(" Rep " + v.representative.name + " ");
    }

} // end aspect MSTKruskal
