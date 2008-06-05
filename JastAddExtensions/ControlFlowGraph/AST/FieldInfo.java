
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public class FieldInfo extends java.lang.Object {
    // Declared in BytecodeDescriptor.jrag at line 36

    private BytecodeParser p;

    // Declared in BytecodeDescriptor.jrag at line 37

    String name;

    // Declared in BytecodeDescriptor.jrag at line 38

    int flags;

    // Declared in BytecodeDescriptor.jrag at line 39

    private FieldDescriptor fieldDescriptor;

    // Declared in BytecodeDescriptor.jrag at line 40

    private Attributes attributes;

    // Declared in BytecodeDescriptor.jrag at line 42


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

    // Declared in BytecodeDescriptor.jrag at line 55



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

    // Declared in BytecodeDescriptor.jrag at line 72


    public boolean isSynthetic() {
      return attributes.isSynthetic();
    }


}
