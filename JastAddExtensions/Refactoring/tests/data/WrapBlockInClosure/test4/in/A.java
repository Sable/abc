class A {
  void m() {
    // here
    {
      System.out.print("Hello, ");
      {
	System.out.println("world!");
      }
    }
  }
}
