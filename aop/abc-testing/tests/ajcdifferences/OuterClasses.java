public aspect OuterClasses {
    before() : staticinitialization(TestOC.*) {
	System.out.println(thisJoinPointStaticPart);
    }
}

class TestOC {
    static class Inner1 { }
    static void foo() { new Runnable() { public void run() { } }; }
    public static void main(String[] args) { foo(); new Inner1(); }
}