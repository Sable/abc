class B {
    class C { }
}

class A extends B {
    void m() {
	C c = new C();
	Object i = (C)c;
	{
	    class C { }
	    System.out.println(i);
	}
    }
}
