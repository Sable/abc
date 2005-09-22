package test;

public aspect Test39 {
  ast A;
 
  public void A.test() {
    test("test1");
  }
  
  // virtual, paramters
  public void A.test(String str) {
  	System.out.println(str);
  }

  public static String str() {
    return "test3";
  }
  
  public String A.result() {
  	return str();
  }
  
  public static void main(String[] args) {
    System.out.println("Intertype declarations: introduce method");
    A a = new A();
    a.test();
    a.test("test2");
    System.out.println(a.result());
  }
}
