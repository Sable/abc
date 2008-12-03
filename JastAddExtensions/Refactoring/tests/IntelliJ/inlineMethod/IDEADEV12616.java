class Test {
  public void foo() {
      String s = /*[*/bar()/*]*/;
  }

  private String bar() {
    String result = null;
    assert result != null;
    return result;
  }
}