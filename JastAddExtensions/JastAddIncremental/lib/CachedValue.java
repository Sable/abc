package AST;

public class CachedValue {
  public final MemoLine line;
  public Object value;
  public CachedValue(ASTNode node) {
    line = new MemoLine(node);
  }
}
