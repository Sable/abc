// Encapsulate/test2/in/A.java p A.C i
package p;

class A {
    void getI(String s) { }
    class C { 
	class B { 
	    void m() { getI(""); } 
	} 
	private int i; 
    }
}
