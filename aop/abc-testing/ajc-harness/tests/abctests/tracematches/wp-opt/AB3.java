import java.util.*;

public aspect AB3 {

	public static int i = 0;
	
	tracematch(Object o) {
    	sym a before: call(* *.a()) && target(o);
		sym x before: call(* *.x()) && target(o);

		a a
		{
			i++;
		}
    }

}
