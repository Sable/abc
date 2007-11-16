package changes;

import AST.ASTNode;

public class NodeReplace extends ASTChange {
    
    private int index;
    private ASTNode before;
    private ASTNode after;
    
    public NodeReplace(ASTNode before, ASTNode after) {
        this.index = before.getParent().getIndexOfChild(before);
        this.before = before;
        this.after = after;
    }

    public String prettyprint() {
        FileRange pos = new FileRange(before.getStart(), before.getEnd());
        return "at "+pos+", replace node "+before.dumpTree()+" with "+after.dumpTree();
    }
    
    public void apply() {
        before.getParent().setChild(after.fullCopy(), index);
    }
    
    public void undo() {
        after.getParent().setChild(before, index);
    }

}
