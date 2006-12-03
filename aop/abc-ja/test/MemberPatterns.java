package test;

import test.MemberPatterns.*;

public class MemberPatterns
{
    public static void main(String[] args)
    {
        System.out.println("A");
        new A().m();
        System.out.println("B");
        new B().m();
        System.out.println("C");
        new C().m();
    }

    static class A { public void m() { } }
    static class B extends A { }
    static class C extends B { public void m() { } }
}

aspect MemberPatternsAspect
{
    before(): call(* A.m()) { System.out.println("call A.m"); }
    before(): call(* B.m()) { System.out.println("call B.m"); }
    before(): call(* C.m()) { System.out.println("call C.m"); }

    before(): execution(* A.m()) { System.out.println("execution A.m"); }
    before(): execution(* B.m()) { System.out.println("execution B.m"); }
    before(): execution(* C.m()) { System.out.println("execution C.m"); }
}
