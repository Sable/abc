
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

	
	public interface Methodoid {
    // Declared in Methodoid.jrag at line 6

		ParameterDeclaration getParameter(int i);

    // Declared in Methodoid.jrag at line 7

		SimpleSet parameterDeclaration(String name);

    // Declared in Methodoid.jrag at line 8

		Block getBlock();

    // Declared in Methodoid.jrag at line 9

		boolean hasBody();

}
