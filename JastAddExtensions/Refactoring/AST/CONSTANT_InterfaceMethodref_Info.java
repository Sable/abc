
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

  public class CONSTANT_InterfaceMethodref_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 145
    public int class_index;

    // Declared in BytecodeCONSTANT.jrag at line 146
    public int name_and_type_index;

    // Declared in BytecodeCONSTANT.jrag at line 148

    public CONSTANT_InterfaceMethodref_Info(BytecodeParser parser) {
      super(parser);
      class_index = p.u2();
      name_and_type_index = p.u2();
    }

    // Declared in BytecodeCONSTANT.jrag at line 154

    public String toString() {
      return "InterfaceMethodRefInfo: " + p.constantPool[class_index] + " "
        + p.constantPool[name_and_type_index];
    }


}
