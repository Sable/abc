// RenameField/test11/in/A.java p A f g
package p;
class A{
	int f;
}
class B extends A{
	A a;
	void m(){
		int g= a.f;
	}
}