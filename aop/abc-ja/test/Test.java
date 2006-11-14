package test;

public class Test {
  public static void main(String[] args) {
    System.out.println("Hello");
    Test test = new Test();
    test.test();
  }
  public void hello() {
    System.out.println("VirtualHello");
  }
}

aspect Aspect {
  public void Test.test() {
    System.out.println("HelloAspect");
    hello();
  }
}
