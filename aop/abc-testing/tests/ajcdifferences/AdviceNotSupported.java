public aspect AdviceNotSupported {
    after() : handler(Throwable) {
	try { System.out.println("foo"); } catch(Throwable e) { 
	    System.out.println("foo"); }
    }
    interface Foo { }
    void around() : staticinitialization(Foo) { }
}

