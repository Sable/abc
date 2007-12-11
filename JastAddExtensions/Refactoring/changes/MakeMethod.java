package changes;

import AST.Block;
import AST.MethodDecl;
import AST.TypeDecl;

public class MakeMethod extends ASTChange {
    
    private TypeDecl td;
    private MethodDecl method;
    private Block host;          // former host block of the method body
    private int index;           // former index of method body in host
    
    public MakeMethod(TypeDecl td, MethodDecl method, Block host, int index) {
        this.td = td;
        this.method = method;
        this.host = host;
        this.index = index;
    }

    public void undo() {
        td.removeBodyDecl(method);
        host.setStmt(method.getBlock(), index);
    }
    
    public String prettyprint() {
        return "add method "+method.dumpTree()+" to type "+td.getID();
    }

}
