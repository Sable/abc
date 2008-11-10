class A {
  void m() throws Exception {
    λ () : void throws Exception {
      System.out.println("Hello!");
      throw new Exception("Bye!");
    }();
  }
  A() {
    super();
  }
}