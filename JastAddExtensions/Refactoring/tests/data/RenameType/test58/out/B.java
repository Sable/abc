package p;

class Sup {
  static int CONSTANT = 0;
  Sup() {
    super();
  }
}

class B extends Sup {
  B() {
    super();
  }
}

class Test {
  public static void main(String[] arguments) {
    System.out.println(B.CONSTANT);
  }
  Test() {
    super();
  }
}
