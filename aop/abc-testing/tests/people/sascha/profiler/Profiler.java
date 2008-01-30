
import sun.misc.Perf;
import java.util.*;

public aspect Profiler 
{
	public static class MethodInfo implements Comparable {
		public int compareTo(Object o) 	{
			return (int) (((MethodInfo)o).realTime-realTime);
		}
		public Object sig;
		public long invocations=0;
		public long time;
		public long realTime;
		
		private static long frequency = p.highResFrequency();
		public long millis(long ticks) { return ticks*1000/frequency; }
		public String toString() { 			
			return  sig + " real: " + millis(realTime) + 
					"ms total: " + millis(time) + 
					"ms invocations: " + invocations; 
		}
	}
	private static Perf p=Perf.getPerf();
	Map /*Signature, MethodInfo*/ functions=new HashMap();
	Object around() : execution(* *.* (..)) && !within(Profiler)
	{	
		long saveSum=sum;
		sum=0;
		long start=p.highResCounter();	
		Object result=proceed();	
		long diff=p.highResCounter()-start;
		long realDiff=diff-sum;

		MethodInfo info=(MethodInfo)functions.get(thisJoinPointStaticPart.getSignature());
		if (info==null) {
			info=new MethodInfo();
			functions.put(thisJoinPointStaticPart.getSignature(), info);
			info.sig=thisJoinPointStaticPart.getSignature();
		}

		info.invocations++;
		info.time+=diff;
		info.realTime+=realDiff;

		long diff2=p.highResCounter()-start;
		sum=saveSum+diff2;		

		return result;
	}
	private long sum;

	after(): execution(void *.main(String[])) {
		List s=new LinkedList(functions.values());
		Collections.sort(s);
		for (Iterator it=s.iterator(); it.hasNext();) {
			MethodInfo info=(MethodInfo)it.next();
			System.out.println(info);
		}		
	}
}