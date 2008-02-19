import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class RunTests {
	protected static boolean verbose;

	public static void main(String[] args) {
    List list = new ArrayList();
    collectFiles(new File("test"), list);
    Collections.sort(list);
    RunTests.verbose = false;
    for(Iterator iter = list.iterator(); iter.hasNext(); ) {
      String name = (String)iter.next();
      runTest(name);
    }
	}
  
	protected static void collectFiles(File f, Collection c) {
		if(f.isFile() && f.getName().endsWith(".result")) {
			try {
        String prefix = new File(".").getCanonicalPath();
        String name = f.getCanonicalPath();
        String testCase = name.substring(prefix.length() + 1, name.length() - 7);
				c.add(testCase);
			} catch (IOException e) {
			}
		}
		else if(f.isDirectory()) {
			File[] files = f.listFiles();
			for(int i = 0; i < files.length; i++)
				collectFiles(files[i], c);
		}
	}

	protected static void runTest(String testName) {
    // check that test case exists
    if(!new File(testName  + ".java").exists())
      return;
    
		System.out.println(testName + ".java");
    
    // redirect output stream
	  PrintStream out = System.out;
    PrintStream err = System.err;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
    System.setErr(ps);
    
    // run JastCompiler to build .class files for test case
		JavaChecker.compile(buildArgs(testName));
    
    // restore output stream
		if(verbose)
			System.out.println(os.toString());
		System.setOut(out);
    System.setErr(err);
    
    // compare output stream result and expected output
		String result = simplifyComparison(os.toString());
		String correct = simplifyComparison(readFile(testName + ".result"));
		if(result.equals(correct)) {
			System.out.println(testName + ".java passed");
		}
		else {
			System.err.println(testName + ".java failed");
			System.err.println("[" + result + "]" + "\nDoes not equal\n" + "[" + correct + "]");
		}
	}
  
  protected static String[] buildArgs(String testName) {
    // parse options file using a stream tokenizer
    ArrayList list = new ArrayList();
    
    // add test case and jastadd run-time sources to command line arguments
    list.add(testName + ".java");

    // create String[] from ArrayList
    String[] args = new String[list.size()];
    int count = 0;
    for(Iterator iter = list.iterator(); iter.hasNext(); count++) {
      String s = (String)iter.next();
      args[count] = s;
    }
    return args;
  }

	protected static String simplifyComparison(String s) {
    // remove leading and trailing whitespace + extra \r added in windows
		s = s.replaceAll("\r", "");
		s = s.trim();
		return s;
	}
	
	protected static String readFile(String name) {
    // return a string with the data in file name
		File file = new File(name);
		int maxsize = 256;
		byte[] bytes = new byte[maxsize];
		try {
			FileInputStream f = new FileInputStream(file);
			int offset = 0;
			int maxread = 0;
			while((maxread = f.available()) > 0) {
        // resize buffer if necessary, double size in each iteration
				while(maxread + offset >= maxsize) {
					byte[] newBytes = new byte[maxsize * 2];
					System.arraycopy(bytes, 0, newBytes, 0, maxsize);
					maxsize *= 2;
					bytes = newBytes;
				}
				offset += f.read(bytes, offset, maxread);
			}
			return new String(bytes);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return "";
	}
}
