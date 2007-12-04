// RenameType/test55/in/A.java p A B
//rename X to XYZ - no ref update
package p;
public class A{
	class X{
		X(X X){new X(null);}
	}
	A(){}
	A(A A){}
	A m(){
		new X(null);
		return (A)new A();
	}
};
class B{
	A.X ax= new A().new X(null);
}