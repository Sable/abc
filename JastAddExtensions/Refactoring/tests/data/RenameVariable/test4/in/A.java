// RenameVariable/test4/in/A.java p A f g
package p;
class A{
	protected int f;
	void m(){
		f++;
	}
}
class B extends A{
	void m(){
		f= 0;
	}
}