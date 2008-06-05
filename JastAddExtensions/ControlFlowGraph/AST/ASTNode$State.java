
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

class ASTNode$State extends java.lang.Object {
    // Declared in ASTNode.ast at line 19

   private int[] stack;

    // Declared in ASTNode.ast at line 20

   private int pos;

    // Declared in ASTNode.ast at line 21

   public ASTNode$State() {
     stack = new int[64];
     pos = 0;
   }

    // Declared in ASTNode.ast at line 25

   private void ensureSize(int size) {
     if(size < stack.length)
       return;
     int[] newStack = new int[stack.length * 2];
     System.arraycopy(stack, 0, newStack, 0, stack.length);
     stack = newStack;
   }

    // Declared in ASTNode.ast at line 32

   public void push(int i) {
     ensureSize(pos+1);
     stack[pos++] = i;
   }

    // Declared in ASTNode.ast at line 36

   public int pop() {
     return stack[--pos];
   }

    // Declared in ASTNode.ast at line 39

   public int peek() {
     return stack[pos-1];
   }


}
