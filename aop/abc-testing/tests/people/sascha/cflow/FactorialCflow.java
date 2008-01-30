

public class FactorialCflow {
    static int fact(int i) {
		return 0;
    }
    public static void main(String args[]) {
    }
}

aspect A {
    pointcut f(int i) : call(int fact(int)) && args(i);

    before(int i, final int j) : f(i) 
        && cflowbelow(cflow(f(j)) && !cflowbelow(f(int))) {
    }
}
