
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;

  public interface Variable {
    // Declared in VariableDeclaration.jrag at line 3

    public String name();

    // Declared in VariableDeclaration.jrag at line 4

    public TypeDecl type();

    // Declared in VariableDeclaration.jrag at line 6

    // 4.5.3
    public boolean isClassVariable();

    // Declared in VariableDeclaration.jrag at line 7

    public boolean isInstanceVariable();

    // Declared in VariableDeclaration.jrag at line 8

    public boolean isMethodParameter();

    // Declared in VariableDeclaration.jrag at line 9

    public boolean isConstructorParameter();

    // Declared in VariableDeclaration.jrag at line 10

    public boolean isExceptionHandlerParameter();

    // Declared in VariableDeclaration.jrag at line 11

    public boolean isLocalVariable();

    // Declared in VariableDeclaration.jrag at line 13

    // 4.5.4
    public boolean isFinal();

    // Declared in VariableDeclaration.jrag at line 15


    public boolean isBlank();

    // Declared in VariableDeclaration.jrag at line 16

    public boolean isStatic();

    // Declared in VariableDeclaration.jrag at line 18


    public TypeDecl hostType();

    // Declared in VariableDeclaration.jrag at line 20


    public Expr getInit();

    // Declared in VariableDeclaration.jrag at line 21

    public boolean hasInit();

    // Declared in VariableDeclaration.jrag at line 23


    public Constant constant();

    // Declared in Liveness.jrag at line 3
    public boolean isLiveBetween(Stmt begin, Stmt end);
    // Declared in Liveness.jrag at line 15
    public boolean isLiveAfter(Stmt stmt);
    // Declared in Liveness.jrag at line 27
    public boolean isLiveAtOrAfter(Stmt stmt);
}
