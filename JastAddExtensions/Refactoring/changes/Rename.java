package changes;

import AST.ASTNode;
import AST.Named;

public class Rename extends ASTChange {

	private Named entity;
    private String old_name;
	private String new_name;
	
	public Rename(Named entity, String new_name) {
		this.entity = entity;
        this.old_name = entity.getID();
		this.new_name = new_name;
	}
	
	public Named getEntity() {
		return entity;
	}
	
	public String getNewName() {
		return new_name;
	}
	
	public String prettyprint() {
		return "change name of entity "+entity+" to "+new_name+
			" at line "+((ASTNode)entity).lineNumber();
	}
    
    public void undo() {
        entity.setID(old_name);
    }
	
}
