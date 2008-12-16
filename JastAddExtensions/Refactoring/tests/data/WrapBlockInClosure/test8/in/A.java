class A {
  void m(boolean b, int i) {
    while(i == 23) {
      System.out.println("56");
      // here
      {
        if(b)
          i = 42;
        else
          break;
      }
    }
  }
}