public class Fibonacci {

    public int fib(int n) {
	if (n <= 1) return 1;
	
	return (fib(n-1) + fib(n-2));
    }

    public void test() {
	System.out.println("fib(4) = " + fib(4));
	System.out.println("fib(8) = " + fib(8));
    }

    public static void main(String[] args) {
	Fibonacci thisfib = new Fibonacci();
	thisfib.test();
    }

}
