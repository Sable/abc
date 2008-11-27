import java.util.List;
class A<T extends java.lang.Object> {
  private int test(List<T> list) {
    return extracted(list);
  }
  protected int extracted(List<T> list) {
    return list.size();
  }
  A() {
    super();
  }
}
