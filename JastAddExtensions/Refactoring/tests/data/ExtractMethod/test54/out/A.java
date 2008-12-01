class A {
  int m() {
    return extracted();
  }
  protected int extracted() {
    try {
      return 23;
    }
    finally {
      System.out.println(42);
    }
  }
  A() {
    super();
  }
}
