/* test case for lifting if(..) out of a cflow, just
 * giving it the mustbinds of the cflow as the parameters
 *
 */

import org.aspectj.testing.Tester;

aspect Aspect {

	static int result = 0;

	before(int x,int y) : cflow(call(* fac(..)) && args(x) && if (x==3)) &&
	                      call (* fac(..)) && args(y) && if (y==1)
			{
				result = 1;
			}
}

public class CflowIf {

	public static int fac(int n) {
		if (n==0) 
			return 1;
		else
			return n*fac(n-1);
	}


	public static void main(String[] args) {
		int dummy = fac(5);
		Tester.checkEqual(Aspect.result, 1, "aspect result");		
	}
}


	
