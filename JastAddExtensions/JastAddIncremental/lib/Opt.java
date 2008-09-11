package AST;
public class Opt extends ASTNode implements Cloneable {
  public Opt() {
    super();
  }

  public Opt(ASTNode opt) {
    setChild(opt, 0);
  }

  public int numChildren() {
    int res = super.numChildren();
    Main.registerDependency(new Dependency(this, 2), numChildren());
    return res;
  }
}
