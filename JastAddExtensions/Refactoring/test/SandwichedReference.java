package test;

public class SandwichedReference {
	class A {
		int i;
	}
	
	class B extends A {
		int j;
		int l = i;
	}
	
	class C extends B {
		int k = i;
	}
	
	class D extends A {
		int m = i;
	}
}
