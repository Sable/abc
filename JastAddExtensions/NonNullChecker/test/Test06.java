package test;

public class Test06 {
  // check raw upto
  @NonNull String name;

  public Test06(@NonNull String s) {
    this.name = s;
    this.m(55);
  }

  @Raw(Test06) void m(int x) {
  }
}

class Test06Sub extends Test06 {
  @NonNull String path;

  public Test06Sub(@NonNull String p, @NonNull String s) {
    super(s);
    this.path = p;
  }

  @Raw(Test06) void m(int x) {
    @NonNull String n = this.path;
    @NonNull String o = this.name;
  }
}
