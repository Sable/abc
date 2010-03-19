package p;

public class ImplicitSuperCtorCall {
	public static ImplicitSuperCtorCall createImplicitSuperCtorCall() {
		return new ImplicitSuperCtorCall();
	}
	protected /*[*/ImplicitSuperCtorCall/*]*/() {
	}
	public static void main(String[] args) {
		System.out.println("Hello world");
		ImplicitSuperCtorCall iscc= createImplicitSuperCtorCall();
	}
}

class B extends ImplicitSuperCtorCall {
}
