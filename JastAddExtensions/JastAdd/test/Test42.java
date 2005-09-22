package test;

public aspect Test42 {
  ast A;

  interface B {
  }

  A implements B;
 
  public static void main(String[] args) {
    System.out.println("Intertype declarations: implement interface");
    Object o = new A();
    System.out.println("Object implements B: " + (o instanceof B));
  }
}
