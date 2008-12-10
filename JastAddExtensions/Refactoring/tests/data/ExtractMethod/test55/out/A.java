class A {
  private void bar() {
    String text = null;
    try {
      extracted();
    }
    catch (Exception e) {
      System.out.println(text);
    }
  }
  protected void extracted() {
    String text;
    text = getString();
  }
  private String getString() {
    return "hello";
  }
  A() {
    super();
  }
}
