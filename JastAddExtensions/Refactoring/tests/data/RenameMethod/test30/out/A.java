package p;
import static java.lang.String.*;

public class A {
  static String valueOf(int i) {
    return "42";
  }
  public static void main(String[] args) {
    System.out.println(String.valueOf(23));
  }
  public A() {
    super();
  }
}
