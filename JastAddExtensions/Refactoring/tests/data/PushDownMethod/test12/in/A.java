// PushDownMethod/test12/in/A.java p A m()
package p;

public class A {
  int x = 23;
  void m() { 
    System.out.println(x);
  }
}

class B extends A {
  int x = 42;
}
