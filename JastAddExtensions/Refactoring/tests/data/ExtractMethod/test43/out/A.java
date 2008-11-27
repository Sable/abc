class A {
  void m() {
    final int i = 42;
    Runnable run;
    run = extracted(i);
    run.run();
  }
  protected Runnable extracted(final int i) {
    Runnable run;
    run = new Runnable() {
        public void run() {
          System.out.println(i);
        }
    };
    return run;
  }
  A() {
    super();
  }
}
