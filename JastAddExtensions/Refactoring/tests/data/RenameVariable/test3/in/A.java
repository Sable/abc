// RenameVariable/test3/in/A.java p A f g
package p;
class A{
	protected int f;
	void m(){
		f++;
	}
}
class B{
	A a;
	protected int f;
	void m(){
		a.f= 0;
	}
}
