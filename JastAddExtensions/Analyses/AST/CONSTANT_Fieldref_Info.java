
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;


  public class CONSTANT_Fieldref_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 78
    public int class_index;

    // Declared in BytecodeCONSTANT.jrag at line 79
    public int name_and_type_index;

    // Declared in BytecodeCONSTANT.jrag at line 81

    public CONSTANT_Fieldref_Info(BytecodeParser parser) {
      super(parser);
      class_index = p.u2();
      name_and_type_index = p.u2();
    }

    // Declared in BytecodeCONSTANT.jrag at line 87

    public String toString() {
      return "FieldRefInfo: " + p.constantPool[class_index] + " "
        + p.constantPool[name_and_type_index];
    }


}
