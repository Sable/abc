// Number.java   aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import  java.util.LinkedList;

public aspect Number 
{
   // ******************************************************************
   // **** Graph class 
   // Executes Number Vertices

   // A point cut to calls to Graph run
   // Gets the pointcuts of the target that call run 
   pointcut graph_run(Graph g, Vertex v): target(g) && args(v) && 
   	call(void Graph.run(Vertex));

    // An after advice to run Vertex Numbering 
    after(Graph g, Vertex v): graph_run(g,v) {
	System.out.println("Running Vertex Numbering ");
	g.NumberVertices();
    }

   // Effectively runs the vertex numbering algorithm
   public void Graph.NumberVertices() {
       NumberWorkSpace nw = new NumberWorkSpace(); 
       GraphSearch(nw);
   }

   // ********************************************************************
   // **** Vertex class
   public int Vertex.VertexNumber;

    // A point cut to calls to Vertex.display
    // Gets the pointcuts of the targets that call display of a Vertex 
    pointcut vertex_display(Vertex v): target(v) && 
	call(void Vertex.display());

    // A before advice to display the vertex number 
    before(Vertex v): vertex_display(v) {
	MyLog.print(" # "+ v.VertexNumber + " ");
    }

   // ******************************************************************
   // **** NumberWorkspace class extends the WorkSpace class
   declare parents: NumberWorkSpace extends WorkSpace;

   int NumberWorkSpace.vertexCounter;

   public NumberWorkSpace.new() {
	   vertexCounter = 0;
   }

   public void NumberWorkSpace.preVisitAction(Vertex v) {
       // This assigns the values on the way in
       if (v.visited!=true) v.VertexNumber = vertexCounter++;
   }

} // of Aspect Number
