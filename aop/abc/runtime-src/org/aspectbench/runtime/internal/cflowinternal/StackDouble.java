package org.aspectbench.runtime.internal.cflowinternal;

public class StackDouble { 
  public static class Cell { 
    public Cell prev;
    public double elem;
    public Cell(Cell prev, double elem) { this.prev = prev; this.elem = elem; }
  }
  
  public Cell top = null;
}
