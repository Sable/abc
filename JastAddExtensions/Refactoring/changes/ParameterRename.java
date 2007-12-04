package changes;

import AST.ASTNode;
import AST.ParameterDeclaration;

public class ParameterRename extends ASTChange {

	private ParameterDeclaration parameter;
    private String old_name;
	private String new_name;
	
	public ParameterRename(ParameterDeclaration parameter, String new_name) {
		this.parameter = parameter;
        this.old_name = parameter.getID();
		this.new_name = new_name;
	}
	
	public ParameterDeclaration getParameter() {
		return parameter;
	}
	
	public String getNewName() {
		return new_name;
	}
	
	public String prettyprint() {
		return "change name of parameter "+parameter.name()+" to "+new_name+
			" at line "+((ASTNode)parameter).lineNumber();
	}
    
    public void apply() {
        old_name = parameter.getID();
        parameter.setID(new_name);
    }
    
    public void undo() {
        parameter.setID(old_name);
    }
	
}
