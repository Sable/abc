package main;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;

public class Test extends Frontend {

    public static void main(String[] args) throws Throwable {
        Test t = new Test();
        t.test("../../tmp/A.java");
    }

    private void test(String file) {
        String[] files = {file};
        if(process(files, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        })) {
            System.out.println("Program: "+program);
        } else {
            System.out.println("unable to process");
        }
    }

}
