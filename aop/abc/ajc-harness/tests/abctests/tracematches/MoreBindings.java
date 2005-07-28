import org.aspectj.testing.Tester;

class PQ {

    void p() {}
    void q() {}
    void r() {}
}


aspect Aspect {

    int matches = 0;

    tracematch(PQ x) {
	sym r before : call(* r(..));
	sym q before : call(* q(..));
	sym p before : call(* p(..)) && target(x);
	(r | p) p
    {
	if (x!=null) matches++;
    }
    }

    after () : execution(* main(..)) {
        Tester.check(matches==1,"1 match");
    }

}

public class MoreBindings {

    public static void main(String[] args) {
	PQ pq = new PQ();
	pq.r(); pq.p(); pq.q(); pq.p();
    }
}

