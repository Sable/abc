// ExtractMethod/test32/in/A.java A m 0 1 n

class A {
  Object m() {
    class B {
    }
    return new B();
  }
}
