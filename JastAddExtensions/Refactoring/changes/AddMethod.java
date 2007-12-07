package changes;

import AST.MethodDecl;
import AST.TypeDecl;

public class AddMethod extends ASTChange {
    
    private TypeDecl td;
    private MethodDecl method;
    
    public AddMethod(TypeDecl td, MethodDecl method) {
        this.td = td;
        this.method = method;
    }

    public void undo() {
        td.removeBodyDecl(method);
    }
    
    public String prettyprint() {
        return "add method "+method.dumpTree()+" to type "+td.getID();
    }

}
