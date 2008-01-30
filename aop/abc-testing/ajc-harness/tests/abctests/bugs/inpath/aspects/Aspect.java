import org.aspectj.testing.Tester;

aspect Aspect
{
    before() : call(* *.foo(..)) {
      Tester.event("before foo");
    }
}
