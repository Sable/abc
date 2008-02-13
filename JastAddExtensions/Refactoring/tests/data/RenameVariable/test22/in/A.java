// RenameVariable/test22/in/A.java args e
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