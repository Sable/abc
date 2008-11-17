package p;

class A {
  {
    System.out.println();
  }
  A() {
    super();
  }
  
  static class Data {
    Data() {
      super();
    }
    private int x;
    private int y;
    int getX() {
      return x;
    }
    int setX(int x) {
      return this.x = x;
    }
    int getY() {
      return y;
    }
    int setY(int y) {
      return this.y = y;
    }
  }
  private Data java = new Data();
  public Data getJava() {
    return java;
  }
  public Data setJava(Data java) {
    return this.java = java;
  }
}
