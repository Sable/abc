package p;
public enum A {
  A0(),

  A2(),

;
public A() {
}
}

class B {
  boolean m(A a) {
    switch (a){
      case A0:
      return true;
      case A2:
      return false;
    }
    return false;
  }
  B() {
    super();
  }
}
