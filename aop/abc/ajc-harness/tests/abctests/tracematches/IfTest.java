import org.aspectj.testing.Tester;

public class IfTest
{
    void aaa(Integer i) { }

    void bbb(Integer j) { }

    public static void main(String[] args)
    {
        IfTest test = new IfTest();

        Integer one = new Integer(1);

        test.aaa(one);
        test.bbb(one);
        test.bbb(one);

        Tester.expectEvent("1");
        Tester.checkAllEvents();
    }
}

aspect TMIfTest
{
    pointcut aaa(IfTest test) : call(void *.aaa(Integer)) && target(test);
    pointcut bbb(IfTest test) : call(void *.bbb(Integer)) && target(test);

    tracematch(IfTest test, Integer i, Integer j)
    {
        sym a1 after  : aaa(test) && args(i);
        sym b1 before : bbb(test) && args(j) && if(j.intValue() == 1);

        a1 b1
        {
            Tester.event(j.toString());
        }
    }
}
