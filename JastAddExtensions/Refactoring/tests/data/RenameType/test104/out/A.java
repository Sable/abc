package p;

public class A {
  public static void main(String[] args) {
    new java.lang.Thread() {
        public void run() {
          System.out.println(23);
        }
    }.start();
  }
  public A() {
    super();
  }
}

class Thread {
  public void start() {
    System.out.println(42);
  }
  Thread() {
    super();
  }
}
