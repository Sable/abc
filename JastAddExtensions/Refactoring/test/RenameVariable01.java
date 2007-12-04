package test;

public class RenameVariable01 extends RenameVariable01Super {
  int i;
  public static void main(String[] args) {
  }
  void v() {
    i = 5;
    int myI = 0;
    i = 6;
    int i;
    i = 3;
  }
  void w() {
    myI = 4;
  }
}

class RenameVariable01Sub extends RenameVariable01 {
  int myI;
  void v() {
    this.i = 3;
  }
}

class RenameVariable01Super {
  int myI;
}
