aspect Instrument {

    pointcut callfib(int x, Object o): execution(int fib(int)) && args(x) && if(x!=-1) && this(o);

     //If the pointcut callfib(x,o) is used within another pointcut: 

    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && callfib(x,o) {
	System.out.println("fib("+y+") from fib("+x+")");
    }
    

     //If the pointcut callfib(x,o) is used within a cflow in another pointcut: 

    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && 
        cflowbelow(callfib(x,o)) {
	System.out.println("fib("+y+") from fib("+x+")");
    }

    // If the pointcut callfib(x,o) is inlined by hand within another pointcut:

    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && 
	execution(int fib(int)) && args(x) && if(x!=-1) && this(o) {
	System.out.println("fib("+y+") from fib("+x+")");
    }

    // If the pointcut callfib(x,o) is inlined by hand within a cflow in another pointcut:

    before(int x, int y, Object o): 
	execution(int fib(int)) && args(y) && 
	cflowbelow(execution(int fib(int)) && args(x) && if(x!=-1) && this(o)) {
	System.out.println("fib("+y+") from fib("+x+")");
    }
    

}
