import org.aspectj.testing.Tester;
import java.util.*;

public aspect CorrectWithinSemantics {
	
	static class C{
		int i;
	}
	
	static Set s1 = Collections.newSetFromMap(new IdentityHashMap());  
	static Set s2 = Collections.newSetFromMap(new IdentityHashMap());  
	static Set s3 = Collections.newSetFromMap(new IdentityHashMap());  
	static Set s4 = Collections.newSetFromMap(new IdentityHashMap());  
	
	public static void main(String args[]) {
		C o = new C() {
			public String toString() { return "o"; }
		};
		
		final C p = new C() {
			public String toString() { return "p"; }
		};
		
		exhibit JP(C o) {
			p.i=3;
		} (o);
		
		Tester.check(!s1.isEmpty(),"'within' did not match for CJP!");
		Tester.check(!s2.isEmpty(),"'withincode' did not match for CJP!");
		Tester.check(!s3.isEmpty(),"'within' did not match for joinpoint within CJP!");
		Tester.check(!s4.isEmpty(),"'withincode' did not match for joinpoint within CJP!");

		Tester.check(s1.contains(o),"'within' did not match correct CJP! "+s1);
		Tester.check(s2.contains(o),"'withincode' did not match correct CJP! "+s2);
		Tester.check(s3.contains(p),"'within' did not match correct joinpoint within CJP! "+s1);
		Tester.check(s4.contains(p),"'withincode' did not match correct joinpoint within CJP! "+s2);

		for(Object i: s1) {
			if(i!=o) {
				Tester.check(false,"superflous match for 'within' (1)");
				break;
			}
		}

		for(Object i: s2) {
			if(i!=o) {
				Tester.check(false,"superflous match for 'withincode' (1)");
				break;
			}
		}

		for(Object i: s3) {
			if(i!=o) {
				Tester.check(false,"superflous match for 'withincode' (2)");
				break;
			}
		}

		for(Object i: s4) {
			if(i!=o) {
				Tester.check(false,"superflous match for 'withincode' (2)");
				break;
			}
		}
}
	
	joinpoint void JP(C j);
	
	before(C o): within(CorrectWithinSemantics) && args(o) && !cflow(adviceexecution()) {
		s1.add(o);
		System.err.println(thisJoinPointStaticPart);
	}
	
	before(C o): withincode(* main(..)) && args(o) {
		s2.add(o);
	}

	before(C o): set(* *) && within(CorrectWithinSemantics) && target(o) && !cflow(adviceexecution()) {
		s3.add(o);
	}
	
	before(C o): set(* *) && withincode(* main(..)) && target(o) {
		s4.add(o);
	}
}