
public class Foo {

    public static void main(String[] args) {
	new Foo().foo();
    }
    public void foo() { System.out.println("foo"); }
}

aspect Bar {
    surround() : call(* foo()) {
	System.out.println("surround before");
    } {
	System.out.println("surround after");
    }

    surround() : call(* foo()) {
	System.out.println("surround 2 before");
    } {
	System.out.println("surround 2 after");
    }

  
}