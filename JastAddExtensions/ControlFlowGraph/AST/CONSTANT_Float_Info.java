
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public class CONSTANT_Float_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 92

    public float value;

    // Declared in BytecodeCONSTANT.jrag at line 94


    public CONSTANT_Float_Info(BytecodeParser parser) {
      super(parser);
      value = p.readFloat();
    }

    // Declared in BytecodeCONSTANT.jrag at line 99


    public String toString() {
      return "FloatInfo: " + Float.toString(value);
    }

    // Declared in BytecodeCONSTANT.jrag at line 103


    public Expr expr() {
      return new FloatingPointLiteral(Float.toString(value));
    }


}
