//Got code from ch06/src/non-public/

public class Test {
    public int _count;

    public void setCount(int count) {
	_count = count;
    }

    public static void main(String[] args) {
	Foo foo = new Foo();
	System.out.println(foo.CONST);
    }
}


class Foo {
    public final int CONST = 56;
    public void set() {
	Test test = new Test();
	test._count = 0;
	test.setCount(5);
    }
}
