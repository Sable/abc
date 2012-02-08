import java.lang.*;

<R> jpi int JP();
<R> jpi R JP1();
<R> jpi R JP2(R a);


class M{
	
	<L> exhibits L JP1() : call(* *(..));
	<H> exhibits H JP2(H i) : call (* *(..)) && argsinv(i);
	
}


