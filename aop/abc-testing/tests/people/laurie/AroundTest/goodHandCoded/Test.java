public class Test {

  public Test() { }

  static int outsidex = 3;
  int a = 12;

  // body ending in a goto
  public int c(int x)
    { int i1;
     Aspect.aspectOf().before$0(); 
     System.out.println("I'm at the beginning of c,  being called with " + x);
    
    if (x != 0)
     { i1 = x * x; }
    else 
     { i1 = 0; }
    
    System.out.println("I'm at the end of c,  returning with " + i1);
    
    return i1;
  }


  public static void main(String args[])
   { int x = 4;

     // one for each around advice that matches in this body
     // if, like below, the same advice matches in more than one place,
     //    only need one of these, can be reused.
     Around1$State state = new Around1$State();  // should this be lazy?

     
     Test t = new Test();
     System.out.println("--- calling c(4) --------");

     int rval;

     // -- beginning of where to weave
     // Aspect.aspectOf().before$0();
     // rval = t.c(x);
     // -- end of where to weave
     //
     Aspect.aspectOf().before$2();
     // Handwoven application of around advice
     Aspect a = Aspect.aspectOf(); 
     state.finished = false; // must set to false
     rval = a.around$1$beforepart(x,state);
     while (!state.finished)
       { rval = t.c(state.arg1);
	 rval = a.around$1$continuepart(state,rval);
       } 

     System.out.println("\n--- calling c(5) ---------");
     int rval2;

     Aspect.aspectOf().before$2();
     // Another handwoven around
     state.finished = false; 
     Aspect a2 = Aspect.aspectOf();
     int tmp = x + 1;
     rval2 = a2.around$1$beforepart(tmp,state);
     while (!state.finished)
       { rval2 = t.c(state.arg1);
	 rval2 = a.around$1$continuepart(state,rval2);
       }

     System.out.println("final values are " + rval + " " + rval2);
   }
}
