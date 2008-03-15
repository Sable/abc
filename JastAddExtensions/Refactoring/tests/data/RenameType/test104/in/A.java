// RenameType/test104/in/A.java MyThread Thread
package p;

public class A {
    public static void main(String[] args) {
	new Thread() {
	    public void run() {
		System.out.println(23);
	    }
	}.start();
    }
}

class MyThread {
    public void start() {
	System.out.println(42);
    }
}