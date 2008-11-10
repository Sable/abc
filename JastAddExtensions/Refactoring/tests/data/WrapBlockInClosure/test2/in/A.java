class A {
  void m() throws Exception {
    // here
    {
      System.out.println("Hello!");
      throw new Exception("Bye!");
    }
  }
}
