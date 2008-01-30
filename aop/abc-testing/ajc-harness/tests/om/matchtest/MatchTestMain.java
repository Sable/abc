import org.aspectj.testing.Tester;

public class MatchTestMain {
    public static int AACallCtr = 0;
    public static int ABCallCtr = 0;
    public static int ACCallCtr=  0;
    public static int BACallCtr = 0;
    public static int BBCallCtr = 0;
    public static int BCCallCtr = 0;    
    
	public static void main(String args[]) {
		MatchTestA a = new MatchTestA();
		MatchTestB b = new MatchTestB();
		a.a();
		a.b();

		b.a();
		b.b();
		
		System.out.println(AspectA.aaCallCtr);
		System.out.println(AspectA.abCallCtr);
		System.out.println(AspectA.baCallCtr);
		System.out.println(AspectA.bbCallCtr);
		
		System.out.println(AspectB.aaCallCtr);
		System.out.println(AspectB.abCallCtr);
		System.out.println(AspectB.baCallCtr);
		System.out.println(AspectB.bbCallCtr);
		
		System.out.println(AspectC.aaCallCtr);
		System.out.println(AspectC.abCallCtr);
		System.out.println(AspectC.baCallCtr);
		System.out.println(AspectC.bbCallCtr);
		
		Tester.checkEqual(AspectA.aaCallCtr, 1, "AspectA.aaCallCtr");
		Tester.checkEqual(AspectA.abCallCtr, 1, "AspectA.abCallCtr");
		Tester.checkEqual(AspectA.baCallCtr, 1, "AspectA.baCallCtr");
		Tester.checkEqual(AspectA.bbCallCtr, 1, "AspectA.bbCallCtr");
		
		Tester.checkEqual(AspectB.aaCallCtr, 1, "AspectB.aaCallCtr");
		Tester.checkEqual(AspectB.abCallCtr, 0, "AspectB.abCallCtr");
		Tester.checkEqual(AspectB.baCallCtr, 1, "AspectB.baCallCtr");
		Tester.checkEqual(AspectB.bbCallCtr, 1, "AspectB.bbCallCtr");
		
		Tester.checkEqual(AspectC.aaCallCtr, 1, "AspectC.aaCallCtr");
		Tester.checkEqual(AspectC.abCallCtr, 0, "AspectC.abCallCtr");
		Tester.checkEqual(AspectC.baCallCtr, 1, "AspectC.baCallCtr");
		Tester.checkEqual(AspectC.bbCallCtr, 0, "AspectC.bbCallCtr");
	}
}
