package p;
class Sup {
  static int CONSTANT = 0;
  Sup(){
    super();
  }
}

class B extends Sup {
  B(){
    super();
  }
}

class Test {
  public static void main(java.lang.String[] arguments) {
    java.lang.System.out.println(p.B.CONSTANT);
  }
  Test(){
    super();
  }
}
