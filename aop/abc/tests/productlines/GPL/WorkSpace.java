// Workspace.java   abstract class
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

// The class is extended by the classes defined in the algorithms that 
// use a search method
 public abstract class WorkSpace {           
      public void init_vertex(Vertex v ) { }
      public void preVisitAction(Vertex v ) {}
      public void postVisitAction(Vertex v ) { }
      public void nextRegionAction(Vertex v ) { }
      public void checkNeighborAction(Vertex vsource, Vertex vtarget) { }
   }
