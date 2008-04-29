// RenameVariable/test41/in/A.java p A.D g f
package p;

class A {
  private class C {
    int f;
  }
  class D extends C {
    int g;
  }
}

class B extends A {
  { new D().f = 23; }
}
