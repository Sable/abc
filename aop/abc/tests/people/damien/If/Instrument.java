aspect Instrument {

    pointcut callfib(int x, Object o): execution(int fib(int)) && args(x) && if(x!=-1) && this(o);

     //If the pointcut callfib(x,o) is used within another pointcut: 
    /*
    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && callfib(x,o) {
	System.out.println("fib("+y+") from fib("+x+")");
    }
    */

     //If the pointcut callfib(x,o) is used within a cflow in another pointcut: 
     // ***** 
     // This compiles but produces unverifiable code
     // The problem seems to be that the if() method created expects arguments
     // (int x, Object o) but is passed x boxed (ie Integer) - i have no idea 
     // why this happens here and not when used outside a cflow

    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && 
        cflowbelow(callfib(x,o)) {
	System.out.println("fib("+y+") from fib("+x+")");
    }

    // If the pointcut callfib(x,o) is inlined by hand within another pointcut:
    /*
    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && 
	execution(int fib(int)) && args(x) && if(x!=-1) && this(o) {
	System.out.println("fib("+y+") from fib("+x+")");
    }
    */
    // If the pointcut callfib(x,o) is inlined by hand within a cflow in another pointcut:
    // ***** 
    // This produces a "Variable y not found in context error" during weaving
    // Looking at the jimple for 3 shows that the if() method actually has formals
    // x, y and o (all the advice formals) - this is OK if inefficient in 3, but
    // here the if() will actually appear inside a cflowsetup advice, which has
    // formals just x and o (the free vars in the cflow) and so it's not happy
    // *****
    /*
    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && 
	cflowbelow(execution(int fib(int)) && args(x) && if(x!=-1) && this(o)) {
	System.out.println("fib("+y+") from fib("+x+")");
    }
    */

}
