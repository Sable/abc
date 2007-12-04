
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;


  public class FieldDescriptor extends java.lang.Object {
    // Declared in BytecodeDescriptor.jrag at line 4
    private BytecodeParser p;

    // Declared in BytecodeDescriptor.jrag at line 5
    String typeDescriptor;

    // Declared in BytecodeDescriptor.jrag at line 7

    public FieldDescriptor(BytecodeParser parser, String name) {
      p = parser;
      int descriptor_index = p.u2();
      typeDescriptor = ((CONSTANT_Utf8_Info) p.constantPool[descriptor_index]).string();
      if(BytecodeParser.VERBOSE)
        p.println("  Field: " + name + ", " + typeDescriptor);
    }

    // Declared in BytecodeDescriptor.jrag at line 15

    public Access type() {
      return new TypeDescriptor(p, typeDescriptor).type();
    }

    // Declared in BytecodeDescriptor.jrag at line 19

    public boolean isBoolean() {
      return new TypeDescriptor(p, typeDescriptor).isBoolean();
    }


}
