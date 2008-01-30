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
package DCM.handleMetrics;

public aspect Metrics  {

  /* intervals for dumping, -1 means never dump during computation 
                            1 means dump every time step, plus debug info */
  static int dumpinterval = -1;

  /* counters */
  static long timesteps = 0; // number of timesteps, each new and method
                             // call incs timesteps

  static int calldepth = 0; // depth of current message call chain
  static String callerhead; // class at head of message call chain

  /* update for each constructor */
  after(): DCM.Pointcuts.applConstructors() 
    { timesteps++;

      /* do dump if necessary */ 
      if ((dumpinterval != -1) && (timesteps % dumpinterval == 0))
        doDump(); 
    }

  void around(): DCM.Pointcuts.applConstructors()
    { boolean countit = thisJoinPoint.getThis() != null;
      String classname =
       thisJoinPoint.getStaticPart().getSignature().
                                getDeclaringType().getName(); 
      if (dumpinterval == 1)
        System.out.println("Constructor of " + classname);

      proceed();

      /* increment relevant record for class of object,
         create a new record if one doesn't yet exist. */ 
      if (!DCM.Data.hasClass(classname)) // no entry for this class yet
        { // get the static DCM from properties file
	  int DCMval = Integer.parseInt(
	       DCM.ClassRelationship.getParameter(classname));
          DCM.Data.insertClass(classname,DCMval); // insert the class 
	}

      if (countit) DCM.Data.updateForConstructorCall(classname);  

    }

  /* after each method application, incr count and check if dump needed */
  after(): DCM.Pointcuts.applExecs()
    { timesteps++;
      if ((dumpinterval != -1) && (timesteps % dumpinterval == 0))
        doDump(); 
    }

  /* update for each method invocation */
  Object around(): DCM.Pointcuts.applExecs() 
    { Object result;
      if (dumpinterval == 1) 
	 System.out.println("Call of " + 
	    thisJoinPoint.getStaticPart().getSignature());
      /* if virtual call, keep context */
      if (thisJoinPoint.getThis() != null) 
        { if (calldepth == 0) /* if first one, store head of chain */
	  { callerhead = thisJoinPoint.getThis().getClass().getName(); 
	    if (dumpinterval == 1) 
	      System.out.println("Caller Head " + callerhead);
	  }
	  calldepth++;
	}

      /* do body */
      result = proceed();

      /* update metrics */
      if (calldepth == 1)
        DCM.Data.updateForHeadCaller();
      if (calldepth > 1)
        DCM.Data.updateForSubsequentCaller(callerhead,calldepth);

      /* end of call chain */
      calldepth = 0;

      return(result);
    }

  /* ouput of data */
  after(): DCM.Pointcuts.dataOutput()
    { doDump(); 
    }

  /* ---------------------    helper methods -------------------------- */

  private static void doDump()
    { 
      DCM.Data.dump();
      System.out.println("Number of exec steps: (" + timesteps + ") " +
                         "Objects live: (" + DCM.Data.totalLive() + ") " +
	                 "DCM(System): (" + DCM.Data.totalDCM()  + ")" );
      System.out.println("***********************************");
    }
}    
