/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2006 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.main;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.text.DecimalFormat;
import polyglot.frontend.Stats;
import polyglot.frontend.Pass;

/** Provide timing for Abc.
 *  @author Laurie Hendren
 *  @date May 24, 2004
 *
 *  This class provides basic timing infrastructure for abc.  It has
 *  three main parts,  one for timing phases in abc,  one for capturing
 *  and displaying times from polyglot, and one for collecting the time
 *  spent in Soot Resolving.
 *
 *  To time the abc phases,  an AbcTimer.start() call must be placed
 *  where timing should start.   
 *  Then AFTER each phase, use a AbcTimer.mark("Phasename"),
 *  to say that this phase has ended.  The time allocated to that phase will
 *  be from the end of the previously marked phase to the time of executing
 *  this mark.    
 *
 *  To get the polyglot timings a call to AbcTimer.storePolyglotStats 
 *  and AbcTimer.storePolyglotPasses must be put into the polyglot code.  
 *  These give the AbcTimer references to a list of Polyglot passes and a
 *  reference to the Stats object containing the timing for each pass.
 *
 *  To collect the time for soot resolving,  each call in the compiler to
 *  the soot resolver must call abcTimer.addToSootResolve(long),  with the
 *  time used for that resolve as the param.
 *
 *  Timings may be printed out using AbcTimer.report(), which
 *  will print the different sorts of timings, depending on the settings of the
 *  abcTimer, polyglotTimer and sootResolverTimer flags in main.Debug.java.
 */

public class AbcTimer {

  private static long laststopped;

  private static long total;

  private static long sootresolve_total = 0;

  private static LinkedList history = new LinkedList();

  private static Stats polyglot_stats;

  private static ArrayList polyglot_passes = null ;
  
  private static Thread reportThread;  

  /** reset all static vars, for rerunning abc */
  public static void reset()
    { sootresolve_total = 0;
      total = 0;
      history = new LinkedList();
      polyglot_stats = null;
      polyglot_passes = null;
    }

  /** store the polyglot passes so we can get times out in correct order */
  public static void storePolyglotPasses(ArrayList l)
    { if (polyglot_passes == null) 
        { polyglot_passes = l;
	}
    }

  /** keep a reference to the polyglot stats */
  public static void storePolyglotStats(Stats stats)
    { polyglot_stats = stats;
    }

  /** keep a total of all time spent in Soot resolving */
  public static void addToSootResolve(long t)
    { sootresolve_total += t;
    }

  /** Initialize the timer */
  public static void start()
    { laststopped = System.currentTimeMillis();
    }

  /** Add a new phase to the history,  name is phasename, time is
   *  time since last mark.
   */
  public static void mark(String phasename)
    { long now = System.currentTimeMillis();
      long phasetime = now - laststopped;
      history.add(new TimerPhase(phasename,phasetime));
      if (Debug.v().timerTrace)
        System.err.println("Finished " + phasename + " in " +
             phasetime + " millisec.");
      total += phasetime; 
      laststopped = now;
    }

  /** Compute percentage of total, make a string with two sig digits
   */
  private static String percent(long passtime)
    { double percent = passtime * 100.0 / total; 
      DecimalFormat percfmt = new DecimalFormat("00.000");
      return("[ " + percfmt.format(percent) + "% ] ");
    }

/**
 * Spawns a thread printing the report any few milliseconds.
 * @param delayBetweenReports the delay in milliseconds
 * @author Eric Bodden
 */
public static void startReportInOwnThread(final long delayBetweenReports) {
	  Runnable r = new Runnable() {
		public void run() {
			while(true) {
				report();
			    long currentPhaseTime = System.currentTimeMillis() - laststopped;
			    System.err.println("Time elapsed since last phase:" + currentPhaseTime);
				  System.err.println(
		            "================================================"); 
				try {
					synchronized(this) {
						wait(delayBetweenReports);
					}
				} catch (InterruptedException e) {
				}
			}
		}		  		  
	  };
	  
	  reportThread = new Thread(r);
	  reportThread.start();	  
  }

 /**
  * Stops the report thread if it is running.
  * @author Eric Bodden
  */ 
  public static void stopReportInOwnThread() {
	  if(reportThread!=null)
		  reportThread.interrupt();	 
  }
  
  /** Print out report of all phases timed so far. Debug.v().abcTimer 
   *  must be set to true for report to be printed.
   */
  public synchronized static void report()
    { if (Debug.v().abcTimer)
	{ System.err.println(
            "================================================"); 
	  System.err.println("Breakdown of abc phases  (total: " + total + 
                             " millisec.)");
	  System.err.println(
            "------------------------------------------------");
	  for (Iterator i = history.iterator(); i.hasNext(); )
	    { TimerPhase next = (TimerPhase) i.next();
              String name = next.name;
	      long time = next.time;
	      System.err.println(percent(time) + name + ":  " + time  );
	    }
	  System.err.println(
            "================================================"); 
        }
      if (Debug.v().polyglotTimer)
	{ if (polyglot_passes != null)
	    { System.err.println(
                "================================================"); 
	      System.err.println("Breakdown for polyglot phases: ");
	      System.err.println("-----------------------------  ");
	      // Iterate through polyglot phases.
	      long total = 0;
	      for (Iterator i = polyglot_passes.iterator(); i.hasNext(); )
	        { Pass pass = (Pass) i.next();  
	          Pass.ID id = pass.id();
	          String name = pass.name();
	          long inclusive_time = polyglot_stats.passTime(id,true);
	          total += inclusive_time;
	          long exclusive_time = polyglot_stats.passTime(id,false);
	          System.err.println(percent(inclusive_time) + name + ":  " 
		                     + inclusive_time);
	        }
	      System.err.println(percent(total) + "ALL  :  " + total);
	      System.err.println(
                  "================================================"); 
	    }
        }
      if (Debug.v().sootResolverTimer)
	{ System.err.println(
            "================================================"); 
          System.err.println("Time spent in Soot resolver: " + 
	                            sootresolve_total);
	  System.err.println(
            "================================================"); 
        }
    }

} // AbcTimer   

class TimerPhase {
    String name;
    long time;
    TimerPhase (String name, long time)
      { this.name = name;
	this.time = time;
      }
    public String toString()
      { return(name + ": " + time + "\n");
      }

} //  TimerPhase
