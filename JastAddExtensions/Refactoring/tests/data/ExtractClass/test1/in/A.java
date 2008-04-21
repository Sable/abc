// ExtractClass/test1/in/A.java p A Data data x y
package p;

class B {
	int data = 0;
}

public class A extends B {
	
	int x = init();
	Data y = new Data();
	
	public void f() {
		int data;
		x = 0;
		y = new Data();
		this.x = 2;
		this.y.z = 3;
	}

	public int init() {
		return 4 + data;
	}
	
	public void g() {
		Data data;
	}
	
}

class Data {
	int z;
}
