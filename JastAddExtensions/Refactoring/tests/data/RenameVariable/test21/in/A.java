// RenameVariable/test21/in/A.java e exc
package p;
class A{
    static void main(String[] args) {
	try {
	    args[23] = "";
	} catch(ArrayIndexOutOfBoundsException e) {
	    e.printStackTrace();
	}
    }
}