package test;

public aspect Test40 {
  ast A;
 
  public static String str4 = "test3";
  
  public String A.str = "test1";
  public String A.str2 = str;
  public static String A.str3 = str4;
  
  public static void main(String[] args) {
    System.out.println("Intertype declarations: introduce field");
    A a1 = new A();
    A a2 = new A();
    System.out.println(a1.str);
    System.out.println(a1.str2);
    System.out.println(a1.str3);
    System.out.println(str4);
    a1.str = "not";
    a2.str = "same";
    if(a1.str == a2.str)
      System.out.println("Error did not expect instance field to be same for different nodes");
    a1.str3 = "are";
    a2.str3 = "same";
    if(a1.str3 != a2.str3)
      System.out.println("Error did expect class fields to be same for different nodes");
  }
}
