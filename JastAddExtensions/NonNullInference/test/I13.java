package test;

public class I13 {
  public static void main(String[] args) {
    System.out.println("overriding: covariant return types, merge rawness and parameters");
  }
}

class A {
  public Object m(Object p) {
    m(null);
    return null;
  }
}

interface B {
  Object m(Object p);
}

class C extends A implements B {
  public Object m(Object p) {
    return new Object();
  }
}

