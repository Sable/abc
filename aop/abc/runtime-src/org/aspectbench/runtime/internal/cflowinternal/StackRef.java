package org.aspectbench.runtime.internal.cflowinternal;

public class StackRef { 
  public static class Cell { 
    public Cell prev;
    public Object elem;
    public Cell(Cell prev, Object elem) { this.prev = prev; this.elem = elem; }
  }
  
  public Cell top = null;
}
