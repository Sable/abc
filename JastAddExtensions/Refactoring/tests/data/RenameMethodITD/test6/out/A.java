package p;
import java.util.*;

class HelloYield {
  HelloYield h;
  public static void main(String[] args) {
    new HelloYield().run();
  }
  void run() {
    itdTest();
  }
  HelloYield() {
    super();
  }
}
aspect A {
  public void HelloYield.itdTest() {
    System.out.println("Hello");
  }
  A() {
    super();
  }
}
