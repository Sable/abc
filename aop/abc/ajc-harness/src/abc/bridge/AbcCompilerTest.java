package abc.bridge;
import junit.framework.TestCase;

import org.aspectj.testing.util.TestUtil;

public class AbcCompilerTest extends TestCase {
	public void testSyntaxError() {
		//LogStream.
		TestUtil.LineStream out= new TestUtil.LineStream();
		
		AbcCompiler.compile(out);
		
		String[] lines= out.getLines();
		assertFalse("check Syntax error has occured", lines[0].indexOf("Syntax error") == -1);
	}
}
