/* 
  ajc bug:
  Erroneous error message when initialising an ITD field in
  an ITD constructor:
  
  Cannot make a static reference to the non-static field A.x

*/



aspect Aspect {


    public int A.x;

    public A.new(int a) {
	    this.x = 3; // Cannot make a static reference to the non-static field A.x
    }
}

class A {

    /* directly putting the constructor here would be fine:

    public A(int a) {
        x = 3;
    } 
    */
}

public class ITA2 {

    public static void main(String[] args) {
	A a = new A(3);
    }

}



