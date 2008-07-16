package p;
import java.io.PrintStream;
import static java.lang.System.*;

public class A {
  static PrintStream out = new PrintStream(System.out) {
      public void println(String s) {
        super.println(42);
      }
  };
  public static void main(String[] args) {
    System.out.println("23");
  }
  public A() {
    super();
  }
}
