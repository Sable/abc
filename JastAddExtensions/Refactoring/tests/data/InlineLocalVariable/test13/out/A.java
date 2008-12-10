public class A {
  int m() {
    new int[]{ 23, 42 }[1] = 72;
    return new int[]{ 23, 42 }[0];
  }
}