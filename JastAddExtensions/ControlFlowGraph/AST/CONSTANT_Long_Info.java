
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public class CONSTANT_Long_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 158

    public long value;

    // Declared in BytecodeCONSTANT.jrag at line 160


    public CONSTANT_Long_Info(BytecodeParser parser) {
      super(parser);
      value = p.readLong();
    }

    // Declared in BytecodeCONSTANT.jrag at line 165


    public String toString() {
      return "LongInfo: " + Long.toString(value);
    }

    // Declared in BytecodeCONSTANT.jrag at line 169


    public Expr expr() {
      //return new LongLiteral(Long.toString(value));
      return new LongLiteral("0x" + Long.toHexString(value));
    }


}
