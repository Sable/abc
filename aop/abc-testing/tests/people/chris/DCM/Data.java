package DCM;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;

import java.io.*;

public class Data {
  /* keys are classnames,  values are DCMrecords */ 
  private static Hashtable data = new Hashtable();
  
  /* keys are Objects, values are classnames*/
  private static Hashtable creators = new Hashtable();
  
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
  public static boolean hasEntry(String str)
    { return(data.containsKey(str)); }

  /* put a new cl in table */
  public static void insertEntry (String str)
    { data.put(str, new DCMrecord());
    }

  /* called in finalizer */
  public static void decrAllocated(String key)
    { DCMrecord r = (DCMrecord) data.get(key);
      r.numlive--;
    }
 

  /* called for constructors */
  public static void updateForConstructorCall(String key)
    { DCMrecord r = (DCMrecord) data.get(key);
      //    incr count by 1
      r.numlive++;
      r.numtotal++;
    }
    
  public static void addObject(Object obj, String key)
  {

    creators.put(obj.toString(), key);
  }
  
  public static String getCreator(Object obj)
  {
    return (String) creators.get(obj.toString()); 
  }

  public static void dump()
   { 
     Iterator i = data.keySet().iterator();
     while(i.hasNext())
     {
       String key = (String) i.next();
       DCMrecord dcmr = (DCMrecord) data.get(key);
       if (dcmr.prevlive != dcmr.numlive)
       {
         out.println(key+" => "+dcmr);
       }
       dcmr.prevlive = dcmr.numlive;
     }
     
   }
}

class DCMrecord {
  long numtotal = 0;
  long numlive = 0;
  
  // values changed since last output??
  long prevlive = 0;
  public String toString() 
    { return( "( total: " + numtotal + " , live: " + numlive + ")");
    }
} // DCM record
