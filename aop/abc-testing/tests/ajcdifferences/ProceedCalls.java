// Fails in abc since proceed is a keyword
public aspect ProceedCalls {

    void proceed() {
	System.out.println("Aspect method proceed");
    }

    void around() : call(void foo()) {
	System.out.println("In around advice");
	proceed();
	this.proceed();
	A.proceed();
	System.out.println("Done");
    }
	
    static class A {
        public static void proceed() {
	    System.out.println("Inner class method proceed");
	}
    }

    static void foo() { 
	System.out.println("In foo");
    }

    public static void main(String[] args) {
	foo();
    }

}
