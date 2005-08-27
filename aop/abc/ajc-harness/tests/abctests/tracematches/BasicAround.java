import org.aspectj.testing.Tester;

public class BasicAround
{
    public Integer foo(Integer i)
    {
        return new Integer(i.intValue() + 2);
    }

    public static void main(String[] args)
    {
        BasicAround test = new BasicAround();
        Integer result;

        test.foo(new Integer(0));
        result = test.foo(new Integer(0));

        Tester.check(result.intValue() == 3, "Around tracematch matched");
    }
}

aspect AroundTM
{
    Integer tracematch(Integer i)
    {
        sym aaa before    : call(Integer *.foo(..));
        sym bbb around(i) : call(Integer *.foo(..)) && args(i);

        aaa bbb
        {
            Integer incremented = new Integer(i.intValue() + 1);
            return proceed(incremented);
        }
    }
}
