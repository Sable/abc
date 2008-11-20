package foo;

public class Bar {

  private String prop;
  public void setProp(String prop) { this.prop = prop; }
  public String get<caret>Prop() { return prop; }

  void foo() {
    setProp("");
    getProp();
  }
}