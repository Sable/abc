import org.aspectj.testing.Tester;

aspect Observer
{
    before(int b):
        call(int Fibonacci.fib(int,int,int)) &&
        private(int count)
        (   args(*, b, count) &&
            private(int b, int a)
            (
                args(b, a, *) &&
                if(count == 0 && b < a) // (check lexically scoped names)
            )
        )
	{
		Tester.event("Base case of recursion reached. fib(10) = " + b);
	}
}
