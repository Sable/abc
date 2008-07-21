module jastadd$framework;
public class Opt extends ASTNode implements Cloneable {
  public Opt() {
    super();
  }

  public Opt(ASTNode opt) {
    setChild(opt, 0);
  }
}
