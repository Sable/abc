public class Test {
    public int y;
    Test(int y) { this.y=y; }

    int foo(int x) {
	System.out.println(y+"foo"+x);
	return x+1;
    }

    int foo(float x) {
	return 0;
    }

    public void start() {
	foo(5);
	new Test(4).foo(3);
	new Test2(6).foo(8);
	bar();
    }

    void bar() {
	System.out.println(y+"bar");
    }

    public static void main(String[] args) {
	new Test(2).start();
    }

}

class Test2 extends Test {
    Test2(int y) { super(y); }
    
    int foo(int x) {
	System.out.println(y+"foo'"+x);
	return x+2;
    }

    void bar() {
	System.out.println(y+"bar'");
    }
}
