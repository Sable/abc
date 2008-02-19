package test;

public class I14 {
  public static void main(String[] args) {
    System.out.println("parameter assingments in constructor invocations");
  }

  public I14() {
    this(null);
  }

  public I14(Object o) {
  }
  public I14(Object o, Object p) {
  }
}

class I14Sub extends I14 {
  public I14Sub() {
    super(null, new Object());
    new I14Sub(null, new Object());
  }
  public I14Sub(Object o, Object p) {
  }
}
