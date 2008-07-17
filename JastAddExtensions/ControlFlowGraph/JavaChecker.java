import AST.*;
import java.util.*;
import java.io.*;

class JavaChecker extends Frontend {

  public static void main(String args[]) {  
    //compile(args);
	  //add
	  try {
		  Vector<String> files = readfiles(args[0]);
		  String[] paths = new String[files.size()];
		  files.toArray(paths);
		  
		  compile(paths);
	  }
	  catch (FileNotFoundException ex) {}			
	  catch (IOException ex) {}
	 	  
  }

  public static boolean compile(String args[]) {
    JavaChecker checker = new JavaChecker();
    boolean result = checker.process(
        args,
        new BytecodeParser(),
        new JavaParser() {
          public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
            return new parser.JavaParser().parse(is, fileName);
          }
        }
    );
    return result;
  }
  protected void initOptions() {
    super.initOptions();
    program.options().addKeyOption("-dot");
  }

  protected void processNoErrors(CompilationUnit unit) {
    System.out.println(unit);
    System.out.println(unit.dumpTree());
    if(program.options().hasOption("-dot"))
      unit.emitDotDescription();
  }
  
  
	public static Vector<String> readfiles(String filepath) throws FileNotFoundException, IOException {
		Vector<String> files = new Vector<String>();
		try {			
			File file = new File(filepath);
			if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + "\\" + filelist[i]);
					if (!readfile.isDirectory()) 
						files.addAll(readfiles(readfile.getPath()));         
					else 
						files.addAll(readfiles(filepath + "\\" + filelist[i]));
				}
			}
			else { 
				String suffix = "";
				if(filepath.contains("."))
					suffix = filepath.substring(filepath.lastIndexOf("."));					
                if(suffix.equals(".java")) 
					files.add(filepath);	
			}
		}
		catch (FileNotFoundException e) {
	    	System.out.println("readfile() Exception:" + e.getMessage());
		}
		
	    return files;
	}	


}
