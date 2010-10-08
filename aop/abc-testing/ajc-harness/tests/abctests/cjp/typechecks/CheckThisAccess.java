public class CheckThisAccess {
	
	CheckThisAccess f;
	static CheckThisAccess s;
	
	void instanceMethod() {
		exhibit A.JP { f = this; }; //should be allowed
	}
	
	static void staticMethod() {
		exhibit A.JP { s = this; }; //error: "this" not defined
	}

	void assign() {
		exhibit A.JP { this = new CheckThisAccess(); }; //error: assigning to "this"
	}
}

aspect A {
	
	joinpoint void JP();
	
}