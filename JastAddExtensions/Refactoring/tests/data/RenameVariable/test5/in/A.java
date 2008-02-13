// RenameVariable/test5/in/A.java p A f g
package p;
class A{
	protected int f;
	void m(){
		f++;
	}
}

class AA extends A{
	protected int f;
}

class B{
	A a;
	void m(){
		a.f= 0;
	}
}