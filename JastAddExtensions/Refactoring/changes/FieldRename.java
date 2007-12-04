package changes;

import AST.ASTNode;
import AST.FieldDeclaration;

public class FieldRename extends ASTChange {

	private FieldDeclaration field;
    private String old_name;
	private String new_name;
	
	public FieldRename(FieldDeclaration field, String new_name) {
		this.field = field;
        this.old_name = field.getID();
		this.new_name = new_name;
	}
	
	public FieldDeclaration getField() {
		return field;
	}
	
	public String getNewName() {
		return new_name;
	}
	
	public String prettyprint() {
		return "change name of field "+field.name()+" to "+new_name+
			" at line "+((ASTNode)field).lineNumber();
	}
    
    public void apply() {
        old_name = field.getID();
        field.setID(new_name);
    }
    
    public void undo() {
        field.setID(old_name);
    }
	
}
