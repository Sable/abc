public class Test {

    void bar() {
	baz();
    }

    void baz() {
	try {
	   System.out.println("foo");
	} catch(Exception e) {
	   System.out.println("foo");
	}

    }

    void foo() {
	bar();
    }

    public static void main(String[] args) {
	new Test2().foo();
    }
}

class Test2 extends Test {}

aspect Aspect {
    before(Test2 x,Test2 y,Test2 z) : execution(void baz()) && cflow(this(x) && target(z)) && target(y)
	{
           System.out.println("before");	
	}
    
}
