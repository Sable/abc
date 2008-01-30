public aspect Aspect2 {
    after() throwing(Test.Exception3 e) : call(void foo(int)) {
	System.out.println(e);
    }

    pointcut blat(int x) : args(x);

    after(Object x,int y) returning(Object e) : call(* foo(*)) 
	&& blat(x) && blat(y) {
	System.out.println(e.toString());
    }
}
