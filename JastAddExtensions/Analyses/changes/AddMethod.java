package changes;

import AST.ClassDecl;
import AST.MethodDecl;

public class AddMethod extends ASTChange {
    
    private ClassDecl clazz;
    private MethodDecl method;
    
    public AddMethod(ClassDecl clazz, MethodDecl method) {
        this.clazz = clazz;
        this.method = method;
    }

    public void apply() {
        clazz.addBodyDecl(method);
    }
    
    public void undo() {
        clazz.getBodyDeclList().remove(method);
    }
    
    public String prettyprint() {
        return "add method "+method.dumpTree()+" to class "+clazz;
    }

}
