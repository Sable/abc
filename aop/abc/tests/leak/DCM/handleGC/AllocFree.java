package DCM.handleGC;

import java.lang.reflect.*;

public aspect AllocFree  {

  /* where to put finalizers */
  declare parents: 
    (* && !java..* && !javax..* && !org..* && !DCM..*) 
    implements DCM.handleGC.Finalize;

  /* intervals for gc, -1 means never explicity call gc during computation */
  static final int gcinterval = -1;

  /* counters */
  static long freed = 0;     // number of objects freed
  static long allocated = 0; // number of objects allocated

  /* add a finalize method to the Finalize interface */
  public void DCM.handleGC.Finalize.finalize() throws Throwable 
    { freed++; // one more object freed
      Class cl = this.getClass();
      if (DCM.Data.hasClass(cl)) // if in hash table
        DCM.Data.decrAllocated(cl); // decrement count for class
    }

  before(Object tgt): DCM.Pointcuts.applConstructors(tgt)
    { // check to see if a GC should be started, and ouput given 
      if ((gcinterval != -1) && (allocated % gcinterval == 0))
        {
	    output();
	}
      // increment counters 
      allocated++;

      Class cl = tgt.getClass();
      // if class not in hash table, add it
      if (!DCM.Data.hasClass(cl))
        DCM.Data.insertClass(cl);

      // increment counter
      DCM.Data.updateForConstructorCall(cl);
    }

  /* output of data */
  after(): DCM.Pointcuts.dataOutput()
    {
	output();
    }

  private static void output() {
      System.gc();
      System.runFinalization();
      DCM.Data.dump();
      DCM.Data.out.println( "Total objects allocated: " + allocated +
			    " Total objects freed: " + freed);
      DCM.Data.out.flush();
     }
}    
