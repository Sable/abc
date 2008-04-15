// ExtractClass/test2/in/A.java p A Data d x
package p;

class B {
	int d = 0;
}

public class A extends B {
	
	int x = init();
	
	public int init() {
		return 4 + d;
	}
	
}
