package DCM;

import java.util.Hashtable;
import java.util.Enumeration;

public class Data {
  /* keys are classnames,  values are DCMrecords */ 
  private static Hashtable data = new Hashtable();

  /* check to see if entry for classname exists yet */
  public static boolean hasClass(String classname)
    { return(data.containsKey(classname)); }

  /* put a new classname in table */
  public static void insertClass (String classname)
    { data.put(classname, new DCMrecord());
    }

  /* called in finalizer */
  public static void decrAllocated(String classname)
    { DCMrecord r = (DCMrecord) data.get(classname);
      r.numlive--;
    }

  /* called for constructors */
  public static void updateForConstructorCall(String classname)
    { DCMrecord r = (DCMrecord) data.get(classname);
      //    incr count by 1
      r.numlive++;
      r.numtotal++;
    }

  public static void dump()
   { System.out.println(data.toString());
   }
}

class DCMrecord {
  long numtotal = 0;
  long numlive = 0;

  public String toString() 
    { return( "( total: " + numtotal + " , live: " + numlive + ")\n");
    }
} // DCM record
