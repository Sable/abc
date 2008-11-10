class A {
  void m() {
    int i = 23;
    {
      if(i == 23) 
        return ;
    }
    System.out.println(i);
  }
  A() {
    super();
  }
}