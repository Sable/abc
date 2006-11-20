package test;

aspect Test01Aspect {
  before(): call(* foo()) {
    System.out.println("Before foo");
  }

  after(int i): call(* bar()) && args(i) {
    System.out.println("After bar " + i);
  }
}

public class Test01 {
  public static void main(String[] args) {
    foo();
    bar(9);
  }
  public static void foo() {
    System.out.println("Foo called");
  }

  public static void bar(int i) {
    System.out.println(i);
  }
}
