
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;


  public class CONSTANT_Integer_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 125
    public int value;

    // Declared in BytecodeCONSTANT.jrag at line 127

    public CONSTANT_Integer_Info(BytecodeParser parser) {
      super(parser);
      value = p.readInt();
    }

    // Declared in BytecodeCONSTANT.jrag at line 132

    public String toString() {
      return "IntegerInfo: " + Integer.toString(value);
    }

    // Declared in BytecodeCONSTANT.jrag at line 136

    public Expr expr() {
      //return new IntegerLiteral(Integer.toString(value));
      return new IntegerLiteral("0x" + Integer.toHexString(value));
    }

    // Declared in BytecodeCONSTANT.jrag at line 140
    public Expr exprAsBoolean() {
      return new BooleanLiteral(value == 0 ? "false" : "true");
    }


}
