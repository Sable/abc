package changes;

import AST.FieldDeclaration;
import AST.Modifier;
import AST.Modifiers;

public class PrivatiseField extends ASTChange {
    
    private FieldDeclaration field;
    private Modifiers old_modifiers;
    
    public PrivatiseField(FieldDeclaration field) {
        this.field = field;
        this.old_modifiers = (Modifiers)field.getModifiers().fullCopy();
    }

    public void undo() {
        field.setModifiers(old_modifiers);
    }

    public String prettyprint() {
        return "make field "+field+" private";
    }

}
