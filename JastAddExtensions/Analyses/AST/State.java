
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;


  public class State extends java.lang.Object {
    // Declared in ASTNode.ast at line 18
   private int[] stack;

    // Declared in ASTNode.ast at line 19
   private int pos;

    // Declared in ASTNode.ast at line 20
   public State() {
     stack = new int[64];
     pos = 0;
   }

    // Declared in ASTNode.ast at line 24
   private void ensureSize(int size) {
     if(size < stack.length)
       return;
     int[] newStack = new int[stack.length * 2];
     System.arraycopy(stack, 0, newStack, 0, stack.length);
     stack = newStack;
   }

    // Declared in ASTNode.ast at line 31
   public void push(int i) {
     ensureSize(pos+1);
     stack[pos++] = i;
   }

    // Declared in ASTNode.ast at line 35
   public int pop() {
     return stack[--pos];
   }

    // Declared in ASTNode.ast at line 38
   public int peek() {
     return stack[pos-1];
   }


}
