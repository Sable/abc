
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public class MethodInfo extends java.lang.Object {
    // Declared in BytecodeDescriptor.jrag at line 114

    private BytecodeParser p;

    // Declared in BytecodeDescriptor.jrag at line 115

    String name;

    // Declared in BytecodeDescriptor.jrag at line 116

    int flags;

    // Declared in BytecodeDescriptor.jrag at line 117

    private MethodDescriptor methodDescriptor;

    // Declared in BytecodeDescriptor.jrag at line 118

    private Attributes attributes;

    // Declared in BytecodeDescriptor.jrag at line 120


    public MethodInfo(BytecodeParser parser) {
      p = parser;
      flags = p.u2();
      if(BytecodeParser.VERBOSE)
        p.print("  Flags: " + Integer.toBinaryString(flags));
      int name_index = p.u2();
      CONSTANT_Info info = p.constantPool[name_index];
      if(info == null || !(info instanceof CONSTANT_Utf8_Info)) {
        System.err.println("Expected CONSTANT_Utf8_Info but found: " + info.getClass().getName());
        //if(info instanceof CONSTANT_Class_Info) {
        //  System.err.println(" found CONSTANT_Class_Info: " + ((CONSTANT_Class_Info)info).name());
        //  name = ((CONSTANT_Class_Info)info).name();
        //}
      } 
      name = ((CONSTANT_Utf8_Info)info).string();
      methodDescriptor = new MethodDescriptor(p, name);
      attributes = new Attributes(p);
    }

    // Declared in BytecodeDescriptor.jrag at line 139


    public BodyDecl bodyDecl() {
      if(isConstructor()) {
        return new ConstructorDecl(
            this.p.modifiers(flags),
            name,
            /*BytecodeParser.isInnerClass ? methodDescriptor.parameterListSkipFirst() :*/ methodDescriptor.parameterList(),
            attributes.exceptionList(),
            new Opt(),
            new Block()
            );
      }
      else {
        return new MethodDecl(
            this.p.modifiers(flags),
            methodDescriptor.type(),
            name,
            methodDescriptor.parameterList(),
            attributes.exceptionList(),
            new Opt(new Block())
            );
      }

    }

    // Declared in BytecodeDescriptor.jrag at line 163


    private boolean isConstructor() {
      return name.equals("<init>");
    }

    // Declared in BytecodeDescriptor.jrag at line 167


    public boolean isSynthetic() {
      return attributes.isSynthetic() || (flags & 0x1000) != 0;
    }


}
