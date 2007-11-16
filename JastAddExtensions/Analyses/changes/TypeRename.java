package changes;

import AST.ASTNode;
import AST.BodyDecl;
import AST.ConstructorDecl;
import AST.Program;
import AST.TypeDecl;

public class TypeRename extends ASTChange {

	private TypeDecl type;
    private String old_name;
	private String new_name;
	
	public TypeRename(TypeDecl type, String new_name) {
		this.type = type;
        this.old_name = type.getID();
		this.new_name = new_name;
	}
	
	public TypeDecl getType() {
		return type;
	}
	
	public String getNewName() {
		return new_name;
	}
	
	public String prettyprint() {
		return "change name of type "+type.name()+" to "+new_name+
			" at line "+((ASTNode)type).lineNumber();
	}
    
    public void apply() {
        old_name = type.getID();
        type.setID(new_name);
        rename_constructors(type, new_name);
    }
    
    public void undo() {
        type.setID(old_name);
        rename_constructors(type, old_name);
    }
    
    private void rename_constructors(TypeDecl type, String name) {
        for(int i=0;i<type.getNumBodyDecl();++i) {
            BodyDecl bd = type.getBodyDecl(i);
            if(bd instanceof ConstructorDecl) {
                ConstructorDecl ctordecl = (ConstructorDecl)bd;
                ctordecl.setID(name);
            }
        }
    }
	
}
