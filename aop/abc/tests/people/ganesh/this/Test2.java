public class Test2 {

    Test2() {
	this(new Integer(5).intValue());
    }

    Test2(int x) {
    }

}

aspect Test2Aspect {
    before(Object x) : call(int intValue()) && this(x) {
    }
}
    
