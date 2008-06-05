
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public class MethodDescriptor extends java.lang.Object {
    // Declared in BytecodeDescriptor.jrag at line 80

    private BytecodeParser p;

    // Declared in BytecodeDescriptor.jrag at line 81

    private String parameterDescriptors;

    // Declared in BytecodeDescriptor.jrag at line 82

    private String typeDescriptor;

    // Declared in BytecodeDescriptor.jrag at line 84


    public MethodDescriptor(BytecodeParser parser, String name) {
      p = parser;
      int descriptor_index = p.u2();
      String descriptor = ((CONSTANT_Utf8_Info) p.constantPool[descriptor_index]).string();
      if(BytecodeParser.VERBOSE)
        p.println("  Method: " + name + ", " + descriptor);
      //String[] strings = descriptor.substring(1).split("\\)");
      //parameterDescriptors = strings[0];
      //typeDescriptor = strings[1];
      int pos = descriptor.indexOf(')');
      parameterDescriptors = descriptor.substring(1, pos);
      typeDescriptor = descriptor.substring(pos+1, descriptor.length());
    }

    // Declared in BytecodeDescriptor.jrag at line 98


    public List parameterList() {
      TypeDescriptor d = new TypeDescriptor(p, parameterDescriptors);
      return d.parameterList();
    }

    // Declared in BytecodeDescriptor.jrag at line 102

    public List parameterListSkipFirst() {
      TypeDescriptor d = new TypeDescriptor(p, parameterDescriptors);
      return d.parameterListSkipFirst();
    }

    // Declared in BytecodeDescriptor.jrag at line 107


    public Access type() {
      TypeDescriptor d = new TypeDescriptor(p, typeDescriptor);
      return d.type();
    }


}
