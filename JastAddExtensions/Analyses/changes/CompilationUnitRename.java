package changes;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import AST.CompilationUnit;

public class CompilationUnitRename extends ASTChange {
    
    private CompilationUnit cu;
    private String old_name;
    private String new_name;
    
    public CompilationUnitRename(CompilationUnit cu, String new_name) {
        this.cu = cu;
        this.old_name = cu.getID();
        this.new_name = new_name;
    }

    public String prettyprint() {
        return "rename compilation unit "+cu.relativeName()+" into "+patch_name(cu.relativeName(), new_name);
    }

    public void apply() {
        cu.setRelativeName(patch_name(cu.relativeName(), new_name));
        cu.setPathName(patch_name(cu.pathName(), new_name));
    }
    
    public void undo() {
        cu.setRelativeName(patch_name(cu.relativeName(), old_name));
        cu.setPathName(patch_name(cu.pathName(), old_name));
    }
    
    private static String patch_name(String path, String name) {
        char pathsep = File.separatorChar;
        int i = path.lastIndexOf(pathsep);
        String relname_head = i == -1 ? "" : path.substring(0, i+1);
        String relname_tail = i == -1 ? path : path.substring(i+1);
        int j = relname_tail.lastIndexOf(".");
        return relname_head + name + relname_tail.substring(j);
    }

}
