/* test case for lifting if(..) out of a cflow, just
 * giving it the mustbinds of the cflow as the parameters
 * second test: with nested cflows - the vars that should appear
 * in an if() are those of the closest enclosing cflowm if any,
 * all the pointcut vars otherwise
 * (also tests correct boxing/unboxing of primitive types in
 * an if() inside a cflow)
 * + sharing: the cflows should be shared, checks that the if()s
 * still work
 *
 */

import org.aspectj.testing.Tester;

aspect Aspect {

	static int res1 = 0;
	static int res2 = 0;
	static int res3 = 0;

	pointcut pc(int x,int y,int z) : 
                                 call (* fac(..)) && args(y) && if (y==1)
			      && cflow(call (* fac(..)) && args(x)
				       && cflowbelow(call(* fac(..)) && args(z)
						     && if(z == 3))
				       && if((z==3) && (x==2)));

	before(int x,int y,int z): pc(x,y,z) {
				res1 = 1;
			}
	before(int a,int b,int c): pc(a,b,c) {
	    res2 = 1;
	}
	before(): pc(int,int,int) {
	    res3 = 1;
	}

}

public class CflowIf2 {

	public static int fac(int n) {
		if (n==0) 
			return 1;
		else
			return n*fac(n-1);
	}


	public static void main(String[] args) {
		int dummy = fac(5);
		Tester.checkEqual(Aspect.res1, 1, "aspect result 1");	       
		Tester.checkEqual(Aspect.res2, 1, "aspect result 2");	
		Tester.checkEqual(Aspect.res3, 1, "aspect result 3");	
	}
}


	
