package test;

public aspect Test41 {
  ast A;
 
  public A.A(String s) {
    this();
    System.out.println(s);
  }
  
  public static void main(String[] args) {
    System.out.println("Intertype declarations: introduce constructor");
    A a1 = new A();
    A a2 = new A("Intertype constructor with extra String argument");
  }
}
