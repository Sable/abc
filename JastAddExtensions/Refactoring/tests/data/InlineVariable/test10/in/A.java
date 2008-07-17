public class A {
  private static final int j = 23;
  static final int i = j;
}

class B extends A {
  int k = i;
}