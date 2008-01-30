import java.util.*;

public aspect AB {

	public static int i = 0;
	
	tracematch(Object o) {
    	sym a before: call(* *.a()) && target(o);
    	sym b before: call(* *.b()) && target(o);
		sym x before: call(* *.x()) && target(o);

		a+ b
		{
			i++;
		}
    }

}
