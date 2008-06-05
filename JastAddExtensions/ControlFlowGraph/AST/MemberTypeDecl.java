
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;


// 8.5 Member Type Declarations

public abstract class MemberTypeDecl extends MemberDecl implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
     @SuppressWarnings({"unchecked", "cast"})  public MemberTypeDecl clone() throws CloneNotSupportedException {
        MemberTypeDecl node = (MemberTypeDecl)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in java.ast at line 3
    // Declared in java.ast line 91

    public MemberTypeDecl() {
        super();


    }

    // Declared in java.ast at line 9


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 12

  public boolean mayHaveRewrite() { return false; }

    // Declared in LookupType.jrag at line 396
 @SuppressWarnings({"unchecked", "cast"})     public abstract TypeDecl typeDecl();
    // Declared in LookupType.jrag at line 392
 @SuppressWarnings({"unchecked", "cast"})     public boolean declaresType(String name) {
        boolean declaresType_String_value = declaresType_compute(name);
        return declaresType_String_value;
    }

    private boolean declaresType_compute(String name) {  return typeDecl().name().equals(name);  }

    // Declared in LookupType.jrag at line 394
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl type(String name) {
        TypeDecl type_String_value = type_compute(name);
        return type_String_value;
    }

    private TypeDecl type_compute(String name) {  return declaresType(name) ? typeDecl() : null;  }

    // Declared in Modifiers.jrag at line 246
 @SuppressWarnings({"unchecked", "cast"})     public boolean isStatic() {
        boolean isStatic_value = isStatic_compute();
        return isStatic_value;
    }

    private boolean isStatic_compute() {  return typeDecl().isStatic();  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
