package DCM.handleGC;

import java.lang.reflect.*;

public aspect AllocFree  {

  /* where to put finalizers */
  declare parents: 
    (* && !java..* && !javax..* && !org..* && !DCM..*) 
    implements DCM.handleGC.Finalize;

  /* intervals for gc, -1 means never explicity call gc during computation */
  static int gcinterval = 1000;

  /* counters */
  static long freed = 0;     // number of objects freed
  static long allocated = 0; // number of objects allocated

  /* add a finalize method to the Finalize interface */
  public void DCM.handleGC.Finalize.finalize() throws Throwable 
    { freed++; // one more object freed
      String classname = this.getClass().getName();
      if (DCM.Data.hasClass(classname)) // if in hash table
        DCM.Data.decrAllocated(classname); // decrement count for class
    }

  before(Object tgt): DCM.Pointcuts.applConstructors(tgt)
    { // check to see if a GC should be started, and ouput given 
      if ((gcinterval != -1) && (allocated % gcinterval == 0))
        { System.gc();
          System.runFinalization();
          DCM.Data.dump();
	}
      // increment counters 
      allocated++;

      String classname = tgt.getClass().getName();
      // if class not in hash table, add it
      if (!DCM.Data.hasClass(classname))
        DCM.Data.insertClass(classname);

      // increment counter
      DCM.Data.updateForConstructorCall(classname);
    }

  /* output of data */
  after(): DCM.Pointcuts.dataOutput()
    { System.out.println( "Total objects allocated: " + allocated +
			  " Total objects freed: " + freed);
    }
}    
