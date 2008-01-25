// RenameType/test42/in/A.java p A B
package p;
class A {
	A(A A){}
	A A(A A){
		A= new A(new A(A));
		return A;
	}
}