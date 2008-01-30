


aspect Aspect {

    public A.new(int a) {
	    super(3,4);
    }

}


class B {
    public int z;

    public B(int p, int q) {
	z = p + q;
    }
}

class A extends B {
    int x;
}

public class ITA0 {
    public static void main(String[] args) {
	A a = new A(5);
    }
}
