import org.aspectj.testing.Tester;

public class MultipleBindingsForAround
{
    public void foo(Object o) { }
    
    public void bar(Integer i)
    {
        Tester.check(i.intValue() == 3, "TM body run for each binding");
    }

    public static void main(String[] args)
    {
        MultipleBindingsForAround test = new MultipleBindingsForAround();
        test.foo(new Object());
        test.foo(new Object());
        test.foo(new Object());
        test.bar(new Integer(0));
    }
}

aspect MultipleBindingsForAroundTM
{
    void tracematch(Object o, Integer i)
    {
        sym foo before    : call(void *.foo(..)) && args(o);
        sym bar around(i) : call(void *.bar(..)) && args(i);

        foo bar
        {
            Integer incremented = new Integer(i.intValue() + 1);
            proceed(incremented);
        }
    }
}
