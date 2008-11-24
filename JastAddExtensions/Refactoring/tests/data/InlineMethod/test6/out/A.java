class A {
  void m() {
    int i = 23;
    {
      int j0 = i++;
      if(j0 == 23) 
        System.out.println("magic number!");
      else 
        System.out.println("something else: " + j0);
    }
  }
  void n(int j) {
    if(j == 23) 
      System.out.println("magic number!");
    else 
      System.out.println("something else: " + j);
  }
  A() {
    super();
  }
}