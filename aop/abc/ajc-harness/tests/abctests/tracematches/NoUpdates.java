import org.aspectj.testing.Tester;

public class NoUpdates
{
    static void foo() { }
    static void bar() { }

    public static void main(String[] args)
    {
        foo();
        bar();
        foo();

        Tester.expectEvent("match");
        Tester.expectEvent("match");
        Tester.checkAllEvents();
    }
}

aspect NoUpdatesTM
{
    tracematch()
    {
        sym foo after : call (void *.foo());
        sym bar after throwing : call (void *.bar());

        foo | bar
        {
            Tester.event("match");
        }
    }
}
