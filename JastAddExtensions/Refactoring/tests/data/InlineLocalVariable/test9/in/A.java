public class A {
  void m() {
    int j = 23;
    final int i = j;
    class B {
      int k = i;
    }
  }
}
