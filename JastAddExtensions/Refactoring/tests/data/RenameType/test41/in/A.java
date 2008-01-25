// RenameType/test41/in/A.java p A B
package p;
public class A {
	A(A A){}
	A A(A A){
		A= new A(new A(A));
		return A;
	}
}