public class Foo {
    static Foo f1 = new Foo(){
	    public String toString() {
		return newMethod();
	    }

	    // moved inside
	    private String newMethod() {
		return "a" + "b";
	    }
	};
    
    static Foo f2 = new Foo(){};
}