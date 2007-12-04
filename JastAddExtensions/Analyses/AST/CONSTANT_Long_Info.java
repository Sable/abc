
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;

  public class CONSTANT_Long_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 160
    public long value;

    // Declared in BytecodeCONSTANT.jrag at line 162

    public CONSTANT_Long_Info(BytecodeParser parser) {
      super(parser);
      value = p.readLong();
    }

    // Declared in BytecodeCONSTANT.jrag at line 167

    public String toString() {
      return "LongInfo: " + Long.toString(value);
    }

    // Declared in BytecodeCONSTANT.jrag at line 171

    public Expr expr() {
      //return new LongLiteral(Long.toString(value));
      return new LongLiteral("0x" + Long.toHexString(value));
    }


}
