public class A {
  public void foo() {
    Object runnable = null;
    Object[] disposeList = null;
    extracted(disposeList, runnable);
  }
  protected void extracted(Object[] disposeList, Object runnable) {
    for(int i = 0; i < disposeList.length; i++) {
      if(disposeList[i] == null) {
        disposeList[i] = runnable;
        return ;
      }
    }
  }
  public A() {
    super();
  }
}
