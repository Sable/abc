public class List extends ASTNode implements Cloneable {
  public List() {
    super();
  }

  public List add(ASTNode node) {
    addChild(node);
    return this;
  }
}
