
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

	
	public interface Scope {
    // Declared in Scope.jadd at line 4

		SimpleSet lookupVariable(String name);

    // Declared in Scope.jadd at line 5

		SimpleSet lookupType(String name);

    // Declared in Scope.jadd at line 6

		TypeDecl surroundingType();

    // Declared in AccessType.jrag at line 26
    public Access accessType(TypeDecl td, boolean ambiguous);
}
