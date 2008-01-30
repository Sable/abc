// Prog.java aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import java.util.LinkedList;
import java.util.Random;
import java.io.*;

public aspect ProgTime
{
    // ****************************************************************
    // Main method for timing purposes
    // arg[0] bench file name
    // arg[1] starting vertex
    public static void main(String[] args) {

    // If wrong number of arguments
    if (args.length !=5) { 
	System.out.println("Use ProgTime n_vertices n_edges max_weight n_reps seed");
	System.exit(1);
    }

    // Step 0: read args
    int num_vertices = new Integer(args[0]).intValue();
    int num_edges = new Integer(args[1]).intValue();
    int max_weight = new Integer(args[2]).intValue();
    int num_repetitions = new Integer(args[3]).intValue();
    int seed = new Integer(args[4]).intValue(); 

    // Step 1: create graph object and init random
    Graph g = new Graph();
    Graph gaux = new Graph();
    Random rand = new Random(seed);
        
    // Step 2: reserves space for vertices, edges and weights
    Vertex V[] = new Vertex[num_vertices];
    Edge E[] = new Edge[num_edges];
    int weights[] = new int[num_edges];
    int startVertices[] = new int[num_edges];
    int endVertices[] = new int[num_edges]; 
         
    // Step 3: creates the vertices objects 
    int i=0;
    for (i=0; i<num_vertices; i++){
        V[i] = new Vertex("v"+i);
        g.addVertex(V[i]);
    }
                  
    // Step 4: creates the edges	
    for(i=0; i<num_edges; i++) {
      int first = Math.abs(rand.nextInt() % num_vertices);
      int second = Math.abs(rand.nextInt() % num_vertices);
      int weight = Math.abs(rand.nextInt() % max_weight);
      startVertices[i] = first;
      endVertices[i] = second;
      weights[i] = weight;
    }
         
    // Step 5: Adds the edges
    for (i=0; i<num_edges; i++)
	{ g.addAnEdge(V[startVertices[i]], V[endVertices[i]],weights[i]);}
    
    // Executes the selected features
    g.startProfile(); 
    for (i=0; i<num_repetitions; i++)
       g.run(g.findsVertex("v0"));
           
    g.stopProfile();
    if (MyLog.dumpgraph) g.display();
    g.resumeProfile();
	    
    // End profiling        
    g.endProfile();

    } // end of main 

}
