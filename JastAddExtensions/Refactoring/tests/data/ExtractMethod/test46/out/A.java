public class A {
  public void foo() {
    Object runnable = null;
    Object[] disposeList = null;
    extracted(runnable, disposeList);
  }
  protected void extracted(Object runnable, Object[] disposeList) {
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
