package abc.ja.jpi.utils;

public class JPIAudit {
	public static class JPI{
		public static int JPI = 0;
		public static int generic = 0;
		public static int global = 0;
		public static int genericGlobal = 0;		
		
		public static int overall(){
			return JPI+generic+global+genericGlobal;
		}
	}
	
	public static class Exhibits{
		public static int exhibits = 0;
		public static int generic = 0;
		public static int seal = 0;	
		
		public static int overall(){
			return exhibits+generic+seal;
		}
	}
	
	public static class PCD{
		public static int thisInv = 0;
		public static int targetInv = 0;
		public static int argsInv = 0;
		public static int global = 0;		
		
		public static int invOverall(){
			return thisInv+targetInv+argsInv;
		}
		
		public static int globalOverall(){
			return global;
		}
		
		public static int overall(){
			return invOverall()+globalOverall();
		}
		
	}
	
	public static class CJPAdvice{
		public static int before = 0;		
		public static int around = 0;		
		public static int after = 0;		
		public static int afterReturning = 0;		
		public static int afterThrowing = 0;		
		
		public static int genericBefore = 0;		
		public static int genericAround = 0;		
		public static int genericAfter = 0;		
		public static int genericAfterReturning = 0;		
		public static int genericAfterThrowing = 0;		

		public static int normalOverall(){
			return after+afterReturning+afterThrowing+around+before;
		}
		
		public static int genericOverall(){
			return genericAfter+genericAfterReturning+genericAfterThrowing+genericAround+genericBefore;
		}
		
		public static int overall(){
			return normalOverall() + genericOverall();
		}
	}	
	
	public static int overall(){
		return JPI.overall() + Exhibits.overall() + PCD.overall() + CJPAdvice.overall();
	}

	public static void printOverall() {
		System.out.println("===JPIs===");
		System.out.println("normal\tgeneric\tglobal\tgenericGlobal\toverall");
		System.out.println(JPI.JPI +"\t"+ JPI.generic +"\t"+ JPI.global +"\t"+ JPI.genericGlobal +"\t"+ JPI.overall());

		System.out.println("===Exhibits===");
		System.out.println("normal\tgeneric\tseal\toverall");		
		System.out.println(Exhibits.exhibits+"\t"+Exhibits.generic+"\t"+Exhibits.seal+"\t"+Exhibits.overall());
		
		System.out.println("===PCD===");
		System.out.println("thisInv\ttargetInv\targsInv\tglobal\toverall");
		System.out.println(PCD.thisInv+"\t"+PCD.targetInv+"\t"+PCD.argsInv+"\t"+PCD.global+"\t"+PCD.overall());
		
		System.out.println("===CJPAdvice===");
		System.out.println("before\taround\tafter\tafter-returning\tafter-throwing\toverall");
		System.out.println(CJPAdvice.before+"\t"+CJPAdvice.around+"\t"+
				CJPAdvice.after+"\t"+CJPAdvice.afterReturning+"\t"+
				CJPAdvice.afterThrowing+"\t"+CJPAdvice.normalOverall());

		System.out.println("===GenericCJPAdvice===");
		System.out.println("generic-before\tgeneric-around\tgeneric-after\tgeneric-after-returning\tgeneric-after-throwing\toverall");
		System.out.println(CJPAdvice.genericBefore+
				"\t"+CJPAdvice.genericAround+"\t"+CJPAdvice.genericAfter+
				"\t"+CJPAdvice.genericAfterReturning+"\t"+
				CJPAdvice.genericAfterThrowing+"\t"+
				CJPAdvice.genericOverall());		
	}
}