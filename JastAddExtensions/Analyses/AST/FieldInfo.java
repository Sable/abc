
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;



  public class FieldInfo extends java.lang.Object {
    // Declared in BytecodeDescriptor.jrag at line 27
    private BytecodeParser p;

    // Declared in BytecodeDescriptor.jrag at line 28
    String name;

    // Declared in BytecodeDescriptor.jrag at line 29
    int flags;

    // Declared in BytecodeDescriptor.jrag at line 30
    private FieldDescriptor fieldDescriptor;

    // Declared in BytecodeDescriptor.jrag at line 31
    private Attributes attributes;

    // Declared in BytecodeDescriptor.jrag at line 33

    public FieldInfo(BytecodeParser parser) {
      p = parser;
      flags = p.u2();
      if(BytecodeParser.VERBOSE)
        p.print("Flags: " + flags);
      int name_index = p.u2();
      name = ((CONSTANT_Utf8_Info) p.constantPool[name_index]).string();

      fieldDescriptor = new FieldDescriptor(p, name);
      attributes = new Attributes(p);
    }

    // Declared in BytecodeDescriptor.jrag at line 46


    public BodyDecl bodyDecl() {
      FieldDeclaration f = new FieldDeclaration(
          this.p.modifiers(flags),
          fieldDescriptor.type(),
          name,
          new Opt()
          );
      if(attributes.constantValue() != null)
        if(fieldDescriptor.isBoolean()) {
          f.setInit(attributes.constantValue().exprAsBoolean());
        }
        else {
          f.setInit(attributes.constantValue().expr());
        }
      return f;
    }

    // Declared in BytecodeDescriptor.jrag at line 63

    public boolean isSynthetic() {
      return attributes.isSynthetic();
    }


}
