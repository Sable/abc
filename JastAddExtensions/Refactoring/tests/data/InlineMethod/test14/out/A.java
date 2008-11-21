class A {
  void m() {
    int i = 23;
    l:{
      l0:{
        int i0 = 23;
        if(i0 == 42) 
          break l0;
        System.out.println("here; i == " + i0);
      }
    }
    System.out.println("back");
  }
  void n(int i) {
    if(i == 42) 
      return ;
    System.out.println("here; i == " + i);
  }
  A() {
    super();
  }
}