package org.aspectbench.runtime.internal.cflowinternal;

public class StackInt { 
  public static class Cell { 
    public Cell prev;
    public int elem;
    public Cell(Cell prev, int elem) { this.prev = prev; this.elem = elem; }
  }
  
  public Cell top = null;
}
