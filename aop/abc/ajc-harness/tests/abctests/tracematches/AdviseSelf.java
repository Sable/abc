import org.aspectj.testing.Tester;

public class AdviseSelf
{
    public void foo(Object o) { }
    
    public void bar(Integer i)
    {
        Tester.event(i.toString());
    }

    public static void main(String[] args)
    {
        AdviseSelf test = new AdviseSelf();
        test.foo(new Object());
        test.bar(new Integer(0));
        Tester.expectEvent("5");
        Tester.expectEvent("5");
        Tester.expectEvent("4");
        Tester.expectEvent("3");
        Tester.expectEvent("2");
        Tester.expectEvent("1");
        Tester.checkAllEvents();
    }
}

aspect AdviseSelfTM
{
    void tracematch(AdviseSelf as, Object o, Integer i)
    {
        sym foo before        : call(void *.foo(..)) && args(o) && target(as);
        sym bar around(as, i) : call(void *.bar(..)) && args(i) && target(as)
                                  && if(i.intValue() < 5);

        foo bar
        {
            int val = i.intValue();
            Integer incremented = new Integer(val + 1);

            as.bar(incremented);
            proceed(as, incremented);
        }
    }
}
