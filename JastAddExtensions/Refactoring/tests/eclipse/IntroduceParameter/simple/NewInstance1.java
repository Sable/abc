//selection: 7, 13, 7, 30
//name: string -> abc
package simple;

public class NewInstance1 {
	public void m(int a) {
		String s= new String("abc");
	}
}

class User {
	public void use() {
		new NewInstance1().m(17);
	}
}
