// RenameVariable/test30/in/A.java p A A1 A0
package p;

public enum A { A1, A2 }

class B {
    boolean m(A a) {
	switch(a) {
	case A1: return true;
	case A2: return false;
	}
		return false;
    }
}