package abc.main;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.text.DecimalFormat;
import polyglot.frontend.Stats;
import polyglot.frontend.Pass;

public class AbcTimer {

  private static long laststopped;

  private static long total;

  private static long sootresolve_total = 0;

  private static LinkedList history = new LinkedList();

  private static Stats polyglot_stats;

  private static ArrayList polyglot_passes = null ;

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

  /** Print out report of all phases timed so far. Debug.v().abcTimer 
   *  must be set to true for report to be printed.
   */
  public static void report()
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

