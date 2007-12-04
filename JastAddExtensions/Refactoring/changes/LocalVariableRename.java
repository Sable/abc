package changes;

import AST.ASTNode;
import AST.VariableDeclaration;

public class LocalVariableRename extends ASTChange {

	private VariableDeclaration variable;
    private String old_name;
	private String new_name;
	
	public LocalVariableRename(VariableDeclaration variable, String new_name) {
		this.variable = variable;
        this.old_name = variable.getID();
		this.new_name = new_name;
	}
	
	public VariableDeclaration getParameter() {
		return variable;
	}
	
	public String getNewName() {
		return new_name;
	}
	
	public String prettyprint() {
		return "change name of variable "+variable.name()+" to "+new_name+
			" at line "+((ASTNode)variable).lineNumber();
	}
    
    public void apply() {
        old_name = variable.getID();
        variable.setID(new_name);
    }

    public void undo() {
        variable.setID(old_name);
    }
	
}
