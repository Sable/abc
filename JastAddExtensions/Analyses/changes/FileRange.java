package changes;

import AST.ASTNode;

public class FileRange {
    
    public int sl, sc, el, ec;
    
    public FileRange(int sl, int sc, int el, int ec) {
        this.sl = sl; this.sc = sc;
        this.el = el; this.ec = ec;
    }
    
    public FileRange(int start, int end) {
        this.sl = ASTNode.getLine(start); this.sc = ASTNode.getColumn(start);
        this.el = ASTNode.getLine(end); this.ec = ASTNode.getColumn(end);
    }
    
    public String toString() {
        return "("+sl+", "+sc+")-("+el+", "+ec+")";
    }

}
