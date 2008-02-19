package test;

public class I10 {
  void test() {
    Object v1 = null;
    Object v2 = new Object();
    if(v1 != null) {
      v2 = v1;
    }
  }

  public static void main(String[] args) {
    System.out.println("guarded possibly null variables may be non-null locally");
  }
}
