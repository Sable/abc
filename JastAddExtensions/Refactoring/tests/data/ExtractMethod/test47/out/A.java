import java.util.List;

class A {
  void m() {
    extracted();
  }
  protected List<?> extracted() {
    return n();
  }
  List<?> n() {
    return null;
  }
  A() {
    super();
  }
}
