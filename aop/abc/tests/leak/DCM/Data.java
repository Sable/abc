package DCM;

import java.util.Hashtable;
import java.util.Enumeration;

import java.io.*;

public class Data {
  /* keys are classnames,  values are DCMrecords */ 
  private static Hashtable data = new Hashtable();

    public static PrintStream out;

    static {
	try {
	    FileOutputStream fos = new FileOutputStream("data.out");
	    out = new PrintStream(fos);
	} catch (IOException e) {
	    System.out.println("Could not open output file!");
	    System.exit(10);
	}
    }

  /* check to see if entry for cl exists yet */
  public static boolean hasClass(Class cl)
    { return(data.containsKey(cl)); }

  /* put a new cl in table */
  public static void insertClass (Class cl)
    { data.put(cl, new DCMrecord());
    }

  /* called in finalizer */
  public static void decrAllocated(Class cl)
    { DCMrecord r = (DCMrecord) data.get(cl);
      r.numlive--;
    }

  /* called for constructors */
  public static void updateForConstructorCall(Class cl)
    { DCMrecord r = (DCMrecord) data.get(cl);
      //    incr count by 1
      r.numlive++;
      r.numtotal++;
    }

  public static void dump()
   { out.println(data.toString());
   }
}

class DCMrecord {
  long numtotal = 0;
  long numlive = 0;

  public String toString() 
    { return( "( total: " + numtotal + " , live: " + numlive + ")\n");
    }
} // DCM record
