package test;

public class I09 {
  public I09() {
    test(this);
    test();
  }
  
  static Object test(Object o) {
    return o;
  }

  Object test() {
    return this;
  }

  public static void main(String[] args) {
    System.out.println("return values can be raw");
  }
}
