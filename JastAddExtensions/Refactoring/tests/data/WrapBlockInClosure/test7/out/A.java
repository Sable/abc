class A {
  int m(boolean b) {
    return (λ () : int {
      if(b) 
        return 23;
      else 
        return 42;
    })@();
  }
  A() {
    super();
  }
}