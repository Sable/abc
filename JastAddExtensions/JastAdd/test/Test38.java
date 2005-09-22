package test;

public aspect Test38 {
  ast A;
 
  // virtual, no parameters, no return type
  public void A.test() {
  	System.out.println("test1");
  }
  
  // virtual, paramters
  public void A.test(String str) {
  	System.out.println(str);
  }

  // virtual, return type
  public String A.result() {
  	return "test3";
  }
  
  // static
  public static void A.staticTest() { 
  	System.out.println("test4");
  }
  
  public static void main(String[] args) {
    System.out.println("Intertype declarations: introduce method");
    A a = new A();
    a.test();
    a.test("test2");
    System.out.println(a.result());
    A.staticTest();
  }
}
