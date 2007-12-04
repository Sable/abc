package p;
 interface I {
  int A = 0;
}

class B {
  int A = p.I.A;
  B(){
    super();
  }
}
