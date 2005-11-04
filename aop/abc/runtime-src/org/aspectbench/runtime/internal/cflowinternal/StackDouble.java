package org.aspectbench.runtime.internal.cflowinternal;

public class StackDouble { 
  public static class Cell { 
    public Cell prev;
    public double elem;
    public Cell(Cell prev, double elem) { this.prev = prev; this.elem = elem; }
    public int depth() {
        if(prev == null) return 1;
        return prev.depth()+1;
    }
  }
  
  public Cell top = null;
}
