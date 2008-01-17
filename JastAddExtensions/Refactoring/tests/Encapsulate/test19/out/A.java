package p;
class A {
  private int i = 3;
  public int j = i;
  A(){
    super();
  }
  public int getI() {
    return i;
  }
  public int setI(int i) {
    return this.i = i;
  }
}
