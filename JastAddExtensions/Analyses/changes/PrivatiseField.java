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

    public void apply() {
        if(field.isPrivate())
            return;
        Modifiers m = field.getModifiers();
        for(int i=0;i<m.getNumModifier();++i) {
            String id = m.getModifier(i).getID();
            if(id.equals("protected") || id.equals("public")) {
                m.setModifier(new Modifier("private"), i);
                return;
            }
        }
        m.addModifier(new Modifier("private"));
    }
    
    public void undo() {
        field.setModifiers(old_modifiers);
    }

    public String prettyprint() {
        return "make field "+field+" private";
    }

}
