
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;


  public class CONSTANT_NameAndType_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 192
    public int name_index;

    // Declared in BytecodeCONSTANT.jrag at line 193
    public int descriptor_index;

    // Declared in BytecodeCONSTANT.jrag at line 195

    public CONSTANT_NameAndType_Info(BytecodeParser parser) {
      super(parser);
      name_index = p.u2();
      descriptor_index = p.u2();
    }

    // Declared in BytecodeCONSTANT.jrag at line 201

    public String toString() {
      return "NameAndTypeInfo: " + name_index + " " + descriptor_index;
    }


}
