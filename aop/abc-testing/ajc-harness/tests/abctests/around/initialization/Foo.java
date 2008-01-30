public class Foo {
    public static void main(String[] args) {
        new Foo();
    }
	public int i=1;
	Foo() 
	{
		System.out.println("Foo()" + i);
	}
}

aspect OPAspect {
    void around(Foo foo) : initialization(Foo.new()) && this(foo) {
		System.out.println("around Foo()" + foo.i);
		if (foo.i!=0)
			throw new RuntimeException();
		proceed(foo);
		System.out.println("around Foo()" + foo.i);
		if (foo.i!=1)
			throw new RuntimeException();

    }
}
