// RenameType/test53/in/A.java p A B
//no ref update
package p;
public class A{
	A(){}
	A(A A){}
	A m(){
		return (A)new A();
	}
};