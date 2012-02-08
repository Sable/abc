import java.lang.*;

jpi int JP();
<R extends Object, Z> jpi R JP(Z b);

aspect As{
	
	<A> exhibits int JP() : call(* *(..));
	<Z extends Integer> exhibits int JP() : call(* *(..));
	

	<L> int around JP(){
		return 1;
	}
	
	<M extends Object, F extends Integer> int around JP(){
		return 1;
	}

}
