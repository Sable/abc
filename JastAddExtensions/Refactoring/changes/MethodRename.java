package changes;

import AST.ASTNode;
import AST.FieldDeclaration;
import AST.MethodDecl;

public class MethodRename extends ASTChange {

	private MethodDecl method;
    private String old_name;
	private String new_name;
	
	public MethodRename(MethodDecl method, String new_name) {
		this.method = method;
        this.old_name = method.getID();
		this.new_name = new_name;
	}
	
	public MethodDecl getField() {
		return method;
	}
	
	public String getNewName() {
		return new_name;
	}
	
	public String prettyprint() {
		return "change name of method "+method.name()+" to "+new_name+
			" at line "+((ASTNode)method).lineNumber();
	}
    
    public void apply() {
        old_name = method.getID();
        method.setID(new_name);
    }
    
    public void undo() {
        method.setID(old_name);
    }
	
}
