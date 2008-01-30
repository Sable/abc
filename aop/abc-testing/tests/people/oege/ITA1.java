/* 
   For the purpose of matching within(..), 
   intertype declarations reside in the aspect.

   In the example below, the first before advice 
   applies to the body of gets1, but not to gets2,
   the field initialisation of A.x, or the constructor 
   A(int).

   The second piece of advice never applies.

*/



aspect Aspect {

    static int A.y = 3;

    public int A.x = A.y;

    before() : (get(* A.x)||get(* A.y)) && within(A)
         { System.out.println("first: "+ thisEnclosingJoinPointStaticPart); }

    before() : execution(* gets2(..)) &&  within(A)
	{ System.out.println("second: "+thisEnclosingJoinPointStaticPart);}

    void A.gets2() {
	this.x = this.x + 1;
    }

    public A.new(int a) {
	 this.z = A.y + a;
    }
}

class A {
    int z;

    void gets1() {
	x = x + 1;
	gets2();
    }
    
}

public class ITA1 {

    public static void main(String[] args) {
	A a = new A(5);
	a.gets1(); a.gets2();
    }

}
