package aaa;

import org.aspectj.testing.Tester;

public class A
{
        global : * : !withincode(* A.bar(..));

        void foo(int x)
        {
                Tester.event("In foo");
        }

        void bar(int x)
        {
                Tester.event("In bar");
                foo(x);
        }

        public static void main(String[] args)
        {
                A g = new A();

                Tester.event("---- foo(0) ------------");
                g.foo(0);

                Tester.event("---- foo(1) ------------");
                g.foo(1);

                Tester.event("---- bar -> foo(0) -----");
                g.bar(0);

                Tester.event("---- bar -> foo(1) -----");
                g.bar(1);

		Tester.expectEvent("---- foo(0) ------------");
		Tester.expectEvent("In foo");
		Tester.expectEvent("---- foo(1) ------------");
		Tester.expectEvent("<advice> Entering foo");
		Tester.expectEvent("In foo");
		Tester.expectEvent("---- bar -> foo(0) -----");
		Tester.expectEvent("In bar");
		Tester.expectEvent("In foo");
		Tester.expectEvent("---- bar -> foo(1) -----");
		Tester.expectEvent("In bar");
		Tester.expectEvent("In foo");
		Tester.checkAllEvents();
        }
}
