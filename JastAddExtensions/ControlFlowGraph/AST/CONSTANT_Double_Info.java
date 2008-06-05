
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public class CONSTANT_Double_Info extends CONSTANT_Info {
    // Declared in BytecodeCONSTANT.jrag at line 59

    public double value;

    // Declared in BytecodeCONSTANT.jrag at line 61


    public CONSTANT_Double_Info(BytecodeParser parser) {
      super(parser);
      value = this.p.readDouble();
    }

    // Declared in BytecodeCONSTANT.jrag at line 66


    public String toString() {
      return "DoubleInfo: " + Double.toString(value);
    }

    // Declared in BytecodeCONSTANT.jrag at line 70


    public Expr expr() {
      return new DoubleLiteral(Double.toString(value));
    }


}
