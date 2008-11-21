class A {
  void m() {
    int i = 23;
    l0:{
      int i0 = 23;
      l:
        while(i0 == 2){
        }
      if(i0 == 42) 
        break l0;
      System.out.println("here; i == " + i0);
    }
    System.out.println("back");
  }
  void n(int i) {
    l:
      while(i == 2){
      }
    if(i == 42) 
      return ;
    System.out.println("here; i == " + i);
  }
  A() {
    super();
  }
}