public class Test {

    static aspect Aspect percflow(this(Object)) {
	
	before() : this(Object) { }
	
    }

}


