package p;

class A {
  static void main(String[] args) {
    try {
      args[23] = "";
    }
    catch (ArrayIndexOutOfBoundsException exc) {
      exc.printStackTrace();
    }
  }
  A() {
    super();
  }
}
