public class A {
  static final int j = 23;
  public static final int i = j;
}

class B {
  int k = A.i;
}