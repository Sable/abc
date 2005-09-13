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
    pointcut aaa() : call(void *.aaa(Integer));
    pointcut bbb() : call(void *.bbb(Integer));

    tracematch(Integer i, Integer j)
    {
        sym a1 after : aaa() && args(i);
        sym b1 before : bbb() && args(j) && if(j.intValue() == 1);

        a1 b1
        {
            Tester.event(j.toString());
        }
    }
}
