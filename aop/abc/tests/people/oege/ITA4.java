/* 

*/




aspect Aspect {

    public int A.x = 7 +  this.z; // reference to instance var of target

    void A.gets2() {
	this.x = this.x + 1;
    }

    } 

class A {
    int z;

    void gets1() {
	x = x + 1;
	gets2();
    }

    public A() {
	z = 5;
    }
    
}

public class ITA4 {

    public static void main(String[] args) {
	A a = new A();
	a.gets1(); a.gets2();
    }

}
