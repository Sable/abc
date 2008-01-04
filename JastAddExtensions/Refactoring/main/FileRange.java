package main;

import AST.ASTNode;

/*
 * FileRange represents a range of characters given by start and end position. This
 * is mostly used in the test programs.
 */

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
