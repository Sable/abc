import org.aspectj.testing.Tester;

public class PerThread
{
    public int matches = 0;

    public void foo() { }
    public void bar() { }
    public void baz() { }

    public static PerThread obj = new PerThread();

    public static void main(String[] args) throws InterruptedException
    {
        Thread one = new Interleave();
        Thread two = new Interleave();

        one.start();
        two.start();

        one.join();
        two.join();

        Tester.check(obj.matches == 2, "One match per thread");
    }
}

class Interleave extends Thread
{
    public void run()
    {
        PerThread obj = PerThread.obj;

        try {
            synchronized (obj) {
                obj.foo();
                obj.notify();
                obj.wait();

                obj.bar();
                obj.notify();
                obj.wait();

                obj.baz();
                obj.notify();
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }
}

aspect PerThreadTM
{
    perthread tracematch()
    {
        sym foo after : call(void *.foo());
        sym bar after : call(void *.bar());
        sym baz after : call(void *.baz());

        foo bar baz
        {
            PerThread.obj.matches++;
        }
    }
}
