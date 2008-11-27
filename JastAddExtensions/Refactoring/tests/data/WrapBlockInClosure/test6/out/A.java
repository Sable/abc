class A {
  void m() {
    (λ () : void {
      int x = 23;
      if(x == 23) 
        return ;
      System.out.println();
    })@();
  }
  A() {
    super();
  }
}