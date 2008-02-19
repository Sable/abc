package test;

public class I08 {
  Object f;
  public I08() {
    f = this;
  }
  
  void test() {
    Object v = f;
  }

  public static void main(String[] args) {
    System.out.println("explicit this in constructor is raw and propagates accordingly");
  }
}
