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

package DCM.handleGC;

import java.lang.reflect.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.Signature;

public aspect AllocFree  {

  /* where to put finalizers */
  declare parents: 
    (* && !java..* && !javax..* && !org..* && !DCM..*) 
    implements DCM.handleGC.Finalize;

  /* intervals for gc, -1 means never explicity call gc during computation */
  static int gcinterval = -1;

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

  /* update for each constructor */
  after(): DCM.Pointcuts.applConstructors() 
    { allocated++;  // incr num allocated
      /* check to see if a GC should be started */
      if ((gcinterval != -1) && (allocated % gcinterval == 0))
        { System.gc();
          System.runFinalization();
	}
    }

  /* output of data */
  after(): DCM.Pointcuts.dataOutput()
    { System.out.println( "Total objects allocated: " + allocated +
			  " Total objects freed: " + freed);
    }
}    
