import org.aspectj.testing.Tester;

public class Fibonacci
{
	static public int fib(int a, int b, int count)
	{
		if (count == 0)
			return b;
		return fib(b, a+b, count-1);
	}

	static public void main(String[] args)
	{
		Tester.event(""+fib(0,1,10));
                Tester.expectEvent("Base case of recursion reached. fib(10) = 89");
		Tester.expectEvent("89");
		Tester.checkAllEvents();
	}
}
