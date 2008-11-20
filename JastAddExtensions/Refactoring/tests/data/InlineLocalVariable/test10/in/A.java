public class A {
    void m() {
	int j = 23;
	final int i = j;
	new Object() {
	    { System.out.println(i); }
	};
    }
}