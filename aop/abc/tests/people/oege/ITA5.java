/* 

*/




aspect Aspect {

    private static int u = 3;

    private static int v = 14;

    public int A.x = 7 +  this.z; // reference to instance var of target

    void A.gets2() {
	System.out.println("gets2; x="+this.x);
	this.x = this.x + super.z;
    }

    public A.new(int a) {
	 super(u,v);
	 System.out.println("A.new(int); z="+this.z+" x= "+this.x);
	 this.x = this.x + 9;
    }

} 

class B {
    int z;
    public B(int p, int q) {
	z = p+q;
    }
    public B() { z = 3; }
}
class A extends B {

    void gets1() {
	System.out.println("gets1; x="+x);
	x = x + 1;
	gets2();
    }

    public A() {
	z = 5;
    }

    public A(int a, int b, int c) {
	this();
    }
}

public class ITA5 {

    public static void main(String[] args) {
	System.out.println("before new");
	A a = new A(5);
	System.out.println("after new");
	a.gets1(); a.gets2();
    }

}




