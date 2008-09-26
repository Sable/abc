import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class RunTests {

  protected final static int MAX_TESTS = 100;
	protected static boolean verbose = false;

	public static void main(String[] args) {
    if(args.length == 1)
      runTest(args[0], false);
    else if(args.length == 2 && args[1].equals("true"))
      runTest(args[0], true);
    else {
      for(int i = 1; i < MAX_TESTS; i++)
		    runTest("test/Test" + i, false);
    }
	}
	
	protected static void runTest(String testName, boolean verbose) {
		RunTests.verbose = verbose;
		runTest(testName);
	}
	
	protected static void runTest(String testName) {
    // check that test case exists
    if(!new File(testName  + ".java").exists())
      return;
    try {
		System.out.println(testName + ".java");
    
    // redirect output stream
	  PrintStream out = System.out;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
    if(!verbose)
		  System.setOut(ps);
    
    // run JastAdd to build .class files for test case
		if(new jastadd.JastAdd().compile(buildArgs(testName))) {
      // load test class in a separate class loader and invoke main method
      String className = testName.replace('/', '.');
      loadAndInvoke(className);
    }
    
    // restore output stream
		if(verbose)
			System.out.println(os.toString());
    else
		  System.setOut(out);
    
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
    } catch (Exception e) {
      e.printStackTrace();
    }
	}
  
  protected static String[] buildArgs(String testName) {
    // parse options file using a stream tokenizer
    ArrayList list = new ArrayList();
    File file = new File(testName + ".options");
    if(file.exists()) {
      try {
        StreamTokenizer st = new StreamTokenizer(new FileInputStream(file));
        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, ' ');
        st.quoteChar('"');
        st.wordChars('0', '9');
        st.wordChars('_', '_');
        st.wordChars('-', '-');
        while(st.nextToken() != StreamTokenizer.TT_EOF) {
          if(st.ttype == StreamTokenizer.TT_WORD) {
            list.add(st.sval);
          }
        }
      } catch (FileNotFoundException e) {
      } catch (IOException e) {
      }
    }
    
    // add test case and jastadd run-time sources to command line arguments
    list.add(testName + ".java");
    list.add("test/ASTNode.java");
    list.add("test/Opt.java");
    list.add("test/List.java");
    
    if(verbose)
      list.add("-verbose");
    
    list.add("-weave_inline");
    list.add("-inh_in_astnode");

    // create String[] from ArrayList
    String[] args = new String[list.size()];
    int count = 0;
    for(Iterator iter = list.iterator(); iter.hasNext(); count++) {
      String s = (String)iter.next();
      args[count] = s;
    }
    return args;
  }

  protected static void loadAndInvoke(String className) {
    // load test class in a separate class loader and invoke main method
    try {
      ClassLoader loader = new URLClassLoader(new URL[] { new File(".").toURL() }, null);
      Class clazz = loader.loadClass(className);
      Method m = clazz.getDeclaredMethod("main", new Class[] { String[].class });
      m.invoke(clazz, new Object[] {new String[] {}});
		} catch (ClassNotFoundException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (MalformedURLException e) {
    }
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
