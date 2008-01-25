// RenameType/test43/in/A.java p A B
//renaming A to B
package p;
public class A {
	static A A;
}
class X extends p.A{
	void x(){
		p.A.A= A.A;//fields come first
	}
}