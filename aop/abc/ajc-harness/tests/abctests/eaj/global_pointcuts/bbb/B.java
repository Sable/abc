package bbb;

import org.aspectj.testing.Tester;

public aspect B
{
    global : B : private(int x) (args(x) && if(x > 0));

    before():
       call(void foo(..))
    {
        Tester.event("<advice> Entering foo");
    }
}
