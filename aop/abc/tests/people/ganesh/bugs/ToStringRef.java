public class ToStringRef extends Base {
    public static void main(String[] args) {
	ToStringRef x=new ToStringRef();
	x.toString();
	x.foo();
	ToStringRef2 y=new ToStringRef2();
	y.toString();
	y.foo();
    }

}

class Base {
    public void foo() {}
}

class ToStringRef2 extends Base {

}