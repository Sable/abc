import org.aspectj.testing.Tester;

public class BindProblem
{  
    public static void main(String args[])
    {
	new BindProblem().foo("first", new Object());
	Tester.expectEvent("first");
	new BindProblem().foo(new Object(), "second");
	Tester.expectEvent("second");
	Tester.checkAllEvents();
    }
    public void foo(Object ob1, Object ob2) {
    }
}

aspect Aspect
{
    before(String s): 
        call(void *.foo*(..)) &&         
		(args(s,..) ||  args(.., s))
    {
	Tester.event(s);
    }
}
