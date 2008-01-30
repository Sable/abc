/* Dynamic Coupling Metrics implemented using AspectJ
 * Copyright (C) 2004 Laurie Hendren, McGill University
 *
 * This code is based on the ideas presented in:
 *     Y. Hassoun, R. Johnson, S. Counsell  A Dynamic Runtime Coupling Metric
 *     for Meta-Level Architectures
 *     Proc. 8th European Conference on Software Maintenance and Reengineering,
 *     Tampere, Finland, March 2004 (CSMR 2004). 
 *
 * and computes the same metric as the code available at:
 *    http://www.dcs.bbk.ac.uk/~yhassoun/downloads/Interceptor.java
 *    http://www.dcs.bbk.ac.uk/~yhassoun/downloads/ClassRelationship.java
 *
 * However, the code is completely rewritten from the original to use a
 * much lighter weight data collection mechanism,  and to include the
 * possibility of accounting for objects being freed by the GC.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

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
  public static void insertClass (String classname, int staticDCMvalue)
    { data.put(classname, new DCMrecord(0, 0, staticDCMvalue, 0));
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
      //    incr dynamic value by static value
      r.dynamicDCMvalue += r.staticDCMvalue;
    }

  /* called when an invoke is found that is the head of a chain of invokes */
  public static void updateForHeadCaller()
    { // all objects get incremented by static value 
      Enumeration allclasses = data.elements();
      // iterate over all classes
      while (allclasses.hasMoreElements())
	 { DCMrecord r = (DCMrecord) allclasses.nextElement();
	   r.dynamicDCMvalue += r.staticDCMvalue * r.numlive;
	 }
    } 

  /* called when an invoke is found that is not the head */
  public static void updateForSubsequentCaller(String classname, int stackdepth)
    { // all objects, except the head caller get incremented by static value 
      // the head caller gets incremented by the number of items 
      // below on task stack
      updateForHeadCaller(); // just add staticDCM for all
      // now fix this up be subtracting static DCM and adding stackdepth -1
      //   to the class of the head. 
      DCMrecord r = (DCMrecord) data.get(classname);
      r.dynamicDCMvalue = r.dynamicDCMvalue - r.staticDCMvalue + 
                              (stackdepth - 1);	
    }

  /* find total live objects over all classes */
  public static long totalLive()
   {  Enumeration allclasses = data.elements();
      long total_live = 0;
      // iterate over all classes
      while (allclasses.hasMoreElements())
	 { DCMrecord r = (DCMrecord) allclasses.nextElement();
	   total_live += r.numlive;
	 }
      return(total_live);
    }

  /* find total dynamic DCM over all classes */
  public static long totalDCM()
   {  Enumeration allclasses = data.elements();
      long totalDCM = 0;
      // iterate over all classes
      while (allclasses.hasMoreElements())
	 { DCMrecord r = (DCMrecord) allclasses.nextElement();
	   totalDCM += r.dynamicDCMvalue;
	 }
      return(totalDCM);
    }

  public static void dump()
   { System.out.println(data.toString());
   }
}

class DCMrecord {
  long numtotal;
  long numlive;
  int staticDCMvalue;
  long dynamicDCMvalue;

  DCMrecord(long ntotal, long nlive, int sDCM, long dDCM)
    { numtotal = ntotal;
      numlive = nlive;
      staticDCMvalue = sDCM;
      dynamicDCMvalue = dDCM;
     }

  public String toString() 
    { return( "( staticDCM: " + staticDCMvalue +
	      " , total: " + numtotal +
	      " , live: " + numlive +
              " , dynamicDCM: " + dynamicDCMvalue + ")\n");
    }
} // DCM record
