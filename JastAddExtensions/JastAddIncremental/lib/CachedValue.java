package AST;

public class CachedValue {
  public final ASTNode node;
  public Object value;
  public CachedValue(ASTNode node) {
    this.node = node;
  }
}
