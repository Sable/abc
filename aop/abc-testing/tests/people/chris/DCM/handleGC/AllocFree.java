package DCM.handleGC;

/*
Aspect to find possible memory leaks.

Weave into abc, polyglot and soot.

abc will then compile the scripts given on the command line 3 times,
and after each cycle will output a list of creation points that still 
have live objects, together with numbers, and write it to data.out

loops are separated by ********after reset markers.

There's a perl script called allocfreesum.pl
which will output a summary of which objects
leak between iterations.
./allocfreesum.pl data.out

*/



import java.lang.reflect.*;
import org.aspectj.lang.*;
public privileged aspect AllocFree  {

  /* where to put finalizers */
  declare parents: 
    (* && !java..* && !javax..* && !org..* && !DCM..* && !java_cup.runtime.Symbol)
  
    implements DCM.handleGC.Finalize;

  /* intervals for gc, -1 means never explicity call gc during computation */
  static final int gcinterval = -1;

  /* counters */
  static long freed = 0;     // number of objects freed
  static long allocated = 0; // number of objects allocated

  private String DCM.handleGC.Finalize.handlegc_creator;
  public String DCM.handleGC.Finalize.handlegc_getCreator()
  {
    return this.handlegc_creator; 
  }
  public String DCM.handleGC.Finalize.handlegc_setCreator(JoinPoint.StaticPart jp, Object newObj)
  {
    String filename = jp.getSourceLocation().getFileName();
      int lineno = jp.getSourceLocation().getLine();

    this.handlegc_creator = filename+"|"+lineno+"|"+newObj.getClass();
    return this.handlegc_creator;
  }
  private int handlegc_copyCallDepth;
  /* add a finalize method to the Finalize interface */
  public void DCM.handleGC.Finalize.finalize() throws Throwable 
      { 


       String key = this.handlegc_getCreator();
       if (key != null && DCM.Data.hasEntry(key)) // if in hash table
      {
        DCM.Data.decrAllocated(this.handlegc_getCreator()); // decrement count for class 
        freed++; // one more object freed
   
      }
      
    }

    
    after() returning (DCM.handleGC.Finalize newObj):  DCM.Pointcuts.constrCall()
    {
       allocated++;
       if ((gcinterval != -1) && (allocated % gcinterval == 0))
        {
  output("after "+allocated+" allocations");	}
      
      String key = newObj.handlegc_setCreator(thisJoinPointStaticPart, newObj);


      // if class not in hash table, add it
      if (!DCM.Data.hasEntry(key))
        DCM.Data.insertEntry(key);

      DCM.Data.updateForConstructorCall(key);
      // increment counter
   
    }
    
    
    Object around():  DCM.Pointcuts.copyCall()
    {
      handlegc_copyCallDepth ++;
      Object newObjTmp = proceed();
      if (handlegc_copyCallDepth == 1 && newObjTmp instanceof DCM.handleGC.Finalize)
      {
       DCM.handleGC.Finalize newObj = (DCM.handleGC.Finalize) newObjTmp;
       allocated++;
       if ((gcinterval != -1) && (allocated % gcinterval == 0))
        {
	    output("after "+allocated+" allocations");
	}
      
      String key = newObj.handlegc_setCreator(thisJoinPointStaticPart, newObj);


      // if class not in hash table, add it
      if (!DCM.Data.hasEntry(key))
        DCM.Data.insertEntry(key);

      DCM.Data.updateForConstructorCall(key);
      // increment counter

   }
   handlegc_copyCallDepth--;
   return newObjTmp;
    }
  
    void around(): DCM.Pointcuts.start()
    {
      for (int i =0;i<3;i++)
      {
        proceed();
        abc.main.Main.reset();
        output("************after reset");
        
        System.err.println("after reset");
      };
    };
  
    
  private static void output(String location) {
      for (int i=0;i<10;++i)
      {
      System.gc();
      System.runFinalization();
      }
      DCM.Data.dump();
      DCM.Data.out.println( location + ": Total objects allocated: " + allocated +
			    " Total objects freed: " + freed + " live " + (allocated-freed));
      DCM.Data.out.flush();
     }
}    
