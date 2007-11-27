
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

	
	// TODO: do we need to make sure that the potential parameters are really
	//       used within the selection? or is that subsumed by the other checks?
	
	public interface LocalDeclaration {
    // Declared in ParameterClassification.jrag at line 7
 
		Access getTypeAccess();

    // Declared in ParameterClassification.jrag at line 8

		String getID();

    // Declared in ParameterClassification.jrag at line 9

		Block getBlock();

    // Declared in ParameterClassification.jrag at line 10

		ParameterDeclaration asParameterDeclaration();

    // Declared in ParameterClassification.jrag at line 11

		VariableDeclaration asVariableDeclaration();

    // Declared in ParameterClassification.jrag at line 33
    public boolean isValueParmFor(Stmt begin, Stmt end);
    // Declared in ParameterClassification.jrag at line 37
    public boolean isOutParmFor(Stmt begin, Stmt end);
    // Declared in ParameterClassification.jrag at line 44
    public boolean mayDefBetween(Stmt begin, Stmt end);
    // Declared in ParameterClassification.jrag at line 53
    public boolean accessedOutside(Stmt begin, Stmt end);
    // Declared in ParameterClassification.jrag at line 56
    public boolean accessedBefore(Stmt stmt);
    // Declared in ParameterClassification.jrag at line 69
    public boolean accessedAfter(Stmt stmt);
    // Declared in ParameterClassification.jrag at line 82
    public boolean shouldMoveOutOf(Stmt begin, Stmt end);
    // Declared in ParameterClassification.jrag at line 85
    public boolean shouldMoveInto(Stmt begin, Stmt end);
    // Declared in ParameterClassification.jrag at line 92
    public boolean shouldDuplicate(Stmt begin, Stmt end);
}
