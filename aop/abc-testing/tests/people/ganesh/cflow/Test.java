public class Test {

    void bar(int x) {
	baz();
    }

    void baz() {
	try {
	   System.out.println("foo");
	} catch(Exception e) {
	   System.out.println("foo");
	}

    }

    void foo(double x) {
	bar(5);
    }

    public static void main(String[] args) {
	new Test2().foo(5.0);
    }
}

class Test2 extends Test {}

aspect Aspect {
    before(Test2 x,Test2 y,Test2 z,int a) : execution(void baz()) && cflow(this(x) && target(z) && args(a)) && target(y)
	{
           System.out.println("before");	
	}
    
}
