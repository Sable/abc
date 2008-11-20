class C {
  static final int j = 23;
}

public class A {
  public static final int i = C.j;
}

class B {
  int k = A.i;
}