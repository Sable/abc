package main;

/*
 * Runs the test cases for accessPackage(), accessType(), and accessField().
 */

import tests.FileRange;
import AST.ASTNode;
import AST.Access;
import AST.Block;
import AST.BytecodeParser;
import AST.CastExpr;
import AST.CompilationUnit;
import AST.Dot;
import AST.Expr;
import AST.FieldDeclaration;
import AST.Frontend;
import AST.JavaParser;
import AST.List;
import AST.ParExpr;
import AST.ParameterDeclaration;
import AST.Program;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import AST.Variable;
import AST.VariableDeclaration;

public class RunAccessTests extends Frontend {

    public static void main(String args[]) {
        runPackageAccessTests();
        runTypeAccessTests();
        runFieldAccessTests();
        //runLocalVariableAccessTests();
    }

    public static void runPackageAccessTests() {
        try {
            String[] testfiles1 = { "Access/test1/Test.java" };
            // Test 1
            FileRange rng1 = new FileRange(11, 1, 13, 1);
            testPackageAccess("Access", rng1, "Access", testfiles1);
            testPackageAccess("Access.test1", rng1, "Access.test1", testfiles1);
            testPackageAccess("Access.test1.pkg1", rng1, "Access.test1.pkg1", testfiles1);
            // Test 2
            FileRange rng2 = new FileRange(12, 5, 12, 20);
            testPackageAccess("Access", rng2, "Access", testfiles1);
            testPackageAccess("Access.test1", rng2, "Access.test1", testfiles1);
            testPackageAccess("Access.test1.pkg1", rng2, "Access.test1.pkg1", testfiles1);
            // Test 3
            FileRange rng3 = new FileRange(11, 5, 11, 20);
            testPackageAccess("Access.test3", rng3, null, "Access/test3/Test.java");
            // Test 4
            testPackageAccess("Access.test4", rng3, "Access.test4", "Access/test4/Test.java");
            // Test 5
            String[] testfiles5 = { "Access/test5/Test.java" };
            FileRange rng5 = new FileRange(13, 9, 13, 30);
            testPackageAccess("Access", rng5, "Access", testfiles5);
            testPackageAccess("Access.test5", rng5, "Access.test5", testfiles5);
            testPackageAccess("Access.test5.pkg1", rng5, "Access.test5.pkg1", testfiles5);
            // Test 6
            String[] testfiles6 = { "Access/test6/Test.java" };
            FileRange rng6 = new FileRange(11, 5, 11, 14);
            testPackageAccess("Access", rng6, null, testfiles6);
            testPackageAccess("Access.test6", rng6, null, testfiles6);
            // Test 7
            FileRange rng7 = new FileRange(12, 5, 12, 16);
            String[] testfiles7 = { "Access/test7/Test.java" };
            testPackageAccess("Access", rng7, null, testfiles7);
            testPackageAccess("Access.test7", rng7, null, testfiles7);
            // Test 8
            FileRange rng8 = new FileRange(13, 9, 13, 14);
            String[] testfiles8 = { "Access/test8/Test.java" };
            testPackageAccess("Access", rng8, "Access", testfiles8);
            testPackageAccess("Access.test8", rng8, "Access.test8", testfiles8);
            testPackageAccess("Access.test8.pkg1", rng8, "Access.test8.pkg1", testfiles8);
            // Test 9
            FileRange rng9 = new FileRange(12, 9, 12, 18);
            String[] testfiles9 = { "Access/test9/Test.java" };
            testPackageAccess("Access", rng9, null, testfiles9);
            testPackageAccess("Access.test9", rng9, null, testfiles9);
            testPackageAccess("Access.test9.pkg1", rng9, null, testfiles9);
            // Test 10
            FileRange rng10 = new FileRange(13, 9, 13, 18);
            String[] testfiles10 = { "Access/test10/Test.java" };
            testPackageAccess("Access", rng10, null, testfiles10);
            testPackageAccess("Access.test10", rng10, null, testfiles10);
            testPackageAccess("Access.test10.pkg1", rng10, null, testfiles10);
            // Test 11
            FileRange rng11 = new FileRange(14, 13, 14, 21);
            String[] testfiles11 = { "Access/test11/Test.java" };
            testPackageAccess("Access", rng11, "Access", testfiles11);
            testPackageAccess("Access.test11", rng11, "Access.test11", testfiles11);
            testPackageAccess("Access.test11.pkg1", rng11, "Access.test11.pkg1", testfiles11);
            // Test 12
            FileRange rng12 = new FileRange(15, 26, 15, 36);
            String[] testfiles12 = { "Access/test12/Test.java" };
            testPackageAccess("Access", rng12, null, testfiles12);
            testPackageAccess("Access.test12", rng12, null, testfiles12);
            testPackageAccess("Access.test12.pkg1", rng12, null, testfiles12);
            // Test 13
            FileRange rng13 = new FileRange(16, 13, 16, 23);
            String[] testfiles13 = { "Access/test13/Test.java" };
            testPackageAccess("Access", rng13, null, testfiles13);
            testPackageAccess("Access.test13", rng13, null, testfiles13);
            testPackageAccess("Access.test13.pkg1", rng13, null, testfiles13);
            // Test 14
            FileRange rng14 = new FileRange(15, 13, 15, 23);
            String[] testfiles14 = { "Access/test14/Test.java" };
            testPackageAccess("Access", rng14, null, testfiles14);
            testPackageAccess("Access.test14", rng14, null, testfiles14);
            testPackageAccess("Access.test14.pkg1", rng14, null, testfiles14);
            // done
            System.out.println("All package access tests passed");
        } catch(TestingException e) {
            System.out.println("Package access test failed: "+e);
        }
    }
    
    public static void runTypeAccessTests() {
        try {
            // Test 15
            testTypeAccess(new FileRange(17, 1, 19, 1), new FileRange(13, 16, 13, 16),
                    new TypeAccess("A"), "Access/test15/Test.java");
            // Test 16
            testTypeAccess(new FileRange(18, 5, 18, 19), new FileRange(12, 19, 12, 19),
                    new TypeAccess("B"), "Access/test16/Test.java");
            // Test 17
            testTypeAccess(new FileRange(14, 5, 16, 5), new FileRange(12, 16, 12, 20),
                    new TypeAccess("A"), "Access/test17/Test.java");
            // Test 18
            testTypeAccess(new FileRange(15, 9, 15, 19), new FileRange(12, 19, 12, 19),
                    new TypeAccess("B"), "Access/test17/Test.java");
            // Test 19
            testTypeAccess(new FileRange(11, 1, 16, 1), new FileRange(14, 17, 14, 19),
                    new TypeAccess("Access.test19", "Test"), 
                    "Access/test19/Test.java");
            // Test 20
            testTypeAccess(new FileRange(12, 6, 12, 40), new FileRange(15, 16, 15, 16),
                    new Dot(new TypeAccess("Test"), new TypeAccess("Foo")), 
                    "Access/test20/Test.java");
            // Test 21
            testTypeAccess(new FileRange(10, 1, 15, 1), new FileRange(13, 16, 13, 16),
                    new TypeAccess("Test"), "Access/test21/Test.java");
            // Test 22
            testTypeAccess(new FileRange(3, 1, 5, 1), new FileRange(13, 16, 13, 16),
                    new TypeAccess("Access.test21.pkg1", "Test"), 
                            "Access/test21/pkg1/Test.java", "Access/test21/Test.java");
            // Test 23
            testTypeAccess(new FileRange(10, 1, 11, 1), new FileRange(14, 5, 14, 7),
                    new TypeAccess("Test"), "Access/test23/Test.java");
            // Test 39
            testTypeAccess(new FileRange(5, 5, 7, 5), new FileRange(16, 7, 16, 9),
                    new TypeAccess("XYZ"), "Access/test39/A.java");
            // Test 40
            testTypeAccess(new FileRange(12, 5, 17, 5), new FileRange(15, 27, 15, 39),
                    new TypeAccess("Inner1"), "Access/test40/Test.java");
            // done
            System.out.println("All type access tests passed.");
        } catch(TestingException e) {
            System.out.println("Type access test failed: "+e);
        }
    }

    public static void runFieldAccessTests() {
        try {
            // Test 24
            testFieldAccess(new FileRange(12, 5, 12, 11), new FileRange(13, 14, 13, 15),
                    new VarAccess("a"), "Access/test24/Test.java");
            // Test 25
            testFieldAccess(new FileRange(12, 5, 12, 11), new FileRange(13, 5, 13, 16),
                    new VarAccess("a"), "Access/test25/Test.java");
            // Test 26
            testFieldAccess(new FileRange(11, 5, 11, 14), new FileRange(16, 7, 16, 15),
                    new Dot(new SuperAccess("super"), new VarAccess("foo")), 
                    "Access/test26/Test.java");
            // Test 27
            testFieldAccess(new FileRange(12, 5, 12, 14), new FileRange(19, 24, 19, 32),
                    new Dot(new SuperAccess("super"), new VarAccess("foo")),
                    "Access/test27/Test.java");
            // Test 28
            testFieldAccess(new FileRange(12, 5, 12, 14), new FileRange(20, 24, 20, 32),
                    new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")), 
                    "Access/test28/Test.java");
            // Test 29
            testFieldAccess(new FileRange(11, 6, 11, 13), new FileRange(16, 14, 16, 25),
                    new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")), 
                    "Access/test29/Test.java");
            // Test 30
            testFieldAccess(new FileRange(12, 6, 12, 13), new FileRange(19, 25, 19, 36),
                    new Dot(new ParExpr(new CastExpr(new TypeAccess("B"), new ThisAccess("this"))), new VarAccess("foo")), 
                    "Access/test30/Test.java");
            // Test 31
            testFieldAccess(new FileRange(12, 5, 12, 14), new FileRange(20, 24, 20, 32),
                    new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")), 
                    "Access/test31/Test.java");
            // Test 32
            testFieldAccess(new FileRange(11, 5, 11, 19), new FileRange(13, 16, 13, 23),
                    new Dot(new ThisAccess("this"), new VarAccess("bar")), 
                    "Access/test32/Test.java");
            // Test 33
            testFieldAccess(new FileRange(11, 5, 11, 19), new FileRange(14, 16, 14, 23),
                    new Dot(new ThisAccess("this"), new VarAccess("bar")), 
                    "Access/test33/Test.java");
            // Test 34
            testFieldAccess(new FileRange(11, 5, 11, 19), new FileRange(18, 16, 18, 23),
                    new Dot(new SuperAccess("super"), new VarAccess("bar")), 
                    "Access/test34/Test.java");
            // Test 35
            testFieldAccess(new FileRange(11, 5, 11, 19), new FileRange(14, 24, 14, 36),
                    new VarAccess("bar"), 
                    "Access/test35/Test.java");
            // Test 36
            testFieldAccess(new FileRange(11, 5, 11, 19), new FileRange(15, 27, 15, 39),
                    new Dot(new TypeAccess("Inner1"), new Dot(new ThisAccess("this"), new VarAccess("bar"))), 
                    "Access/test36/Test.java");
            // Test 37
            testFieldAccess(new FileRange(11, 5, 11, 12), new FileRange(17, 24, 17, 26),
                    new VarAccess("bar"),
                    "Access/test37/Test.java");
            // Test 38
            testFieldAccess(new FileRange(12, 5, 12, 12), new FileRange(19, 24, 19, 41),
                    new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new Dot(new TypeAccess("Test"), new ThisAccess("this")))),
                            new VarAccess("bar")),
                    "Access/test38/Test.java");
            // done
            System.out.println("All field access tests passed.");
        } catch(TestingException e) {
            System.out.println("Field access test failed: "+e);
        }
    }
    
    public static void runLocalVariableAccessTests() {
        try {
            // Test 43
            testLocalVariableAccess(new FileRange(11, 14, 11, 21), new FileRange(12, 16, 12, 19),
                    new VarAccess("test"), "Access/test43/Test.java");
            // Test 44
            testLocalVariableAccess(new FileRange(12, 9, 12, 16), new FileRange(14, 16, 14, 19),
                    new VarAccess("test"), "Access/test44/Test.java");
            // Test 45
            testLocalVariableAccess(new FileRange(12, 9, 12, 16), new FileRange(16, 24, 16, 27),
                    new VarAccess("test"), "Access/test45/Test.java");
            // Test 46
            testLocalVariableAccess(new FileRange(12, 9, 12, 22), new FileRange(16, 24, 16, 27),
                    null, "Access/test46/Test.java");
            // done
            System.out.println("All local variable access tests passed.");
        } catch(TestingException e) {
            System.out.println("Local variable access test failed: "+e);
        }
    }
    
    public static void testPackageAccess(String pkgname, FileRange rng, String expected,
            String... files) throws TestingException {
        RunAccessTests b = new RunAccessTests();
        if(b.process(files, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        })) {
            Program p = b.program;
            String filename = files[0];
            ASTNode n = findSmallestCoveringNode(p, filename, rng);
            //System.out.println("node: "+n.dumpTree()+" of type "+n.getClass());           
            if(n == null) {
                throw new TestingException("no node an file "+files[0]+" "+rng);
            } else {
                Access res = n.accessPackage(pkgname);
                if(expected == null) {
                    if(res != null)
                        throw new TestingException("when accessing package "+pkgname+" from "+
                                "file "+files[0]+" at "+rng+": expected failure, got "+res);
                } else {
                    if(res == null)
                        throw new TestingException("when accessing package "+pkgname+" from "+
                                "file "+files[0]+" at "+rng+": expected "+expected+", got failure");
                    else if(!res.toString().equals(expected))
                        throw new TestingException("when accessing package "+pkgname+" from " +
                                "file "+files[0]+" at "+rng+": expected "+expected+", got "+res);
                }
            }
        } else {
            throw new TestingException("cannot compile");
        }
    }


    public static void testTypeAccess(FileRange tprng, FileRange obsrng, Access expected,
            String... files) throws TestingException {
        RunAccessTests b = new RunAccessTests();
        if(b.process(files, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        })) {
            Program p = b.program;
            String tpfile = files[0];
            String obsfile = files.length > 1 ? files[1] : files[0];
            ASTNode m = findSmallestCoveringNode(p, tpfile, tprng);
            if(!(m instanceof TypeDecl))
                throw new TestingException("no type declaration found in file "+tpfile+" at "+tprng);
            ASTNode n = findSmallestCoveringNode(p, obsfile, obsrng);
            //System.out.println("node: "+n.dumpTree()+" of type "+n.getClass());           
            if(n == null) {
                throw new TestingException("no node an file "+files[0]+" "+obsrng);
            }
            Access res = ((Access)n).accessType((TypeDecl)m);
            if(expected == null) {
                if(res != null) {
                    throw new TestingException("when accessing type "+((TypeDecl)m).getID()+" from "+
                            "file "+obsfile+" at "+tprng+": expected failure, got "+res.dumpTree());
                } 
            } else {
                if(res == null)
                    throw new TestingException("when accessing type "+((TypeDecl)m).getID()+" from "+
                            "file "+obsfile+" at "+tprng+": expected "+expected.dumpTree()+", got failure");
                else if(!res.dumpTree().equals(expected.dumpTree()))
                    throw new TestingException("when accessing type "+((TypeDecl)m).getID()+" from " +
                            "file "+obsfile+" at "+tprng+": expected "+expected.dumpTree()+", got "+res.dumpTree());
            }
        } else {
            throw new TestingException("cannot compile");
        }
    }

    public static void testFieldAccess(FileRange fldrng, FileRange obsrng, Access expected,
            String... files) throws TestingException {
        RunAccessTests b = new RunAccessTests();
        if(b.process(files, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        })) {
            Program p = b.program;
            String fldfile = files[0];
            String obsfile = files.length > 1 ? files[1] : files[0];
            ASTNode m = findSmallestCoveringNode(p, fldfile, fldrng);
            if(!(m instanceof FieldDeclaration))
                throw new TestingException("no field declaration found in file "+fldfile+" at "+fldrng);
            ASTNode n = findSmallestCoveringNode(p, obsfile, obsrng);
            //System.out.println("node: "+n.dumpTree()+" of type "+n.getClass());           
            if(n == null) {
                throw new TestingException("no node an file "+files[0]+" "+obsrng);
            }
            Access res;
            if(n instanceof FieldDeclaration)
            	res = ((FieldDeclaration)n).accessField((FieldDeclaration)m);
            else if(n instanceof Block)
            	res = ((Block)n).accessField((FieldDeclaration)m);
            else if(n instanceof List)
            	res = ((TypeDecl)n.getParent()).accessField((FieldDeclaration)m);
            else
            	res = ((Expr)n).accessField((FieldDeclaration)m);
            if(expected == null) {
                if(res != null) {
                    throw new TestingException("when accessing field "+((FieldDeclaration)m).getID()+" from "+
                            "file "+obsfile+" at "+fldrng+": expected failure, got "+res.dumpTree());
                }
            } else {
                if(res == null)
                    throw new TestingException("when accessing field "+((FieldDeclaration)m).getID()+" from "+
                            "file "+obsfile+" at "+fldrng+": expected "+expected.dumpTree()+", got failure");
                else if(!res.dumpTree().equals(expected.dumpTree()))
                    throw new TestingException("when accessing field "+((FieldDeclaration)m).getID()+" from " +
                            "file "+obsfile+" at "+fldrng+": expected "+expected.dumpTree()+", got "+res.dumpTree());
            }
        } else {
            throw new TestingException("cannot compile");
        }
    }

    public static void testLocalVariableAccess(FileRange lvarrng, FileRange obsrng, Access expected,
            String... files) throws TestingException {
        RunAccessTests b = new RunAccessTests();
        if(b.process(files, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        })) {
            Program p = b.program;
            ASTNode m = findSmallestCoveringNode(p, files[0], lvarrng);
            ASTNode n = findSmallestCoveringNode(p, files[0], obsrng);
            //System.out.println("node: "+n.dumpTree()+" of type "+n.getClass());           
            if(n == null) {
                throw new TestingException("no node in file "+files[0]+" "+obsrng);
            }
            Access res;
            if(m instanceof VariableDeclaration) {
                res = n.accessLocalVariable((VariableDeclaration)m);
            } else if(m instanceof ParameterDeclaration) {
                res = n.accessParameter((ParameterDeclaration)m);
            } else {
                throw new TestingException("no local variable or parameter declaration "+
                        "found in file "+files[0]+" at "+lvarrng);
            }
            if(expected == null) {
                if(res != null) {
                    throw new TestingException("when accessing "+((Variable)m).name()+" from "+
                            "file "+files[0]+" at "+lvarrng+": expected failure, got "+res.dumpTree());
                }
            } else {
                if(res == null)
                    throw new TestingException("when accessing field "+((Variable)m).name()+" from "+
                            "file "+files[0]+" at "+lvarrng+": expected "+expected.dumpTree()+", got failure");
                else if(!res.dumpTree().equals(expected.dumpTree()))
                    throw new TestingException("when accessing field "+((FieldDeclaration)m).getID()+" from " +
                            "file "+files[0]+" at "+lvarrng+": expected "+expected.dumpTree()+", got "+res.dumpTree());
            }
        } else {
            throw new TestingException("cannot compile");
        }
    }

    static ASTNode findSmallestCoveringNode(ASTNode r, String filename, FileRange rng) {
        if(r instanceof CompilationUnit && 
                (((CompilationUnit)r).relativeName() == null ||
                        !((CompilationUnit)r).relativeName().equals(filename))) {
            return null;
        }
        for(int i=0;i<r.getNumChild();++i) {
        	ASTNode child = r.getChild(i);
            ASTNode tmp = findSmallestCoveringNode(child, filename, rng);
            if(tmp != null)
                return tmp;
        }
        if(!covers(r, rng))
            return null;
        return r;
    }

    static boolean covers(ASTNode r, FileRange rng) {
        int start = r.getStart();
        int end = r.getEnd();
        return covers(ASTNode.getLine(start), ASTNode.getColumn(start),
                ASTNode.getLine(end), ASTNode.getColumn(end),
                rng.sl, rng.sc, rng.el, rng.ec);
    }

    static boolean covers(int sl1, int sc1, int el1, int ec1, int sl2, int sc2, int el2, int ec2) {
        if(sl1 > sl2 || sl1 == sl2 && sc1 > sc2) return false;
        if(el1 < el2 || el1 == el2 && ec1 < ec2) return false;
        return true;
    }
    
}
