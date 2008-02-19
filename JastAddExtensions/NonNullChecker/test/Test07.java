package test;

public class Test07 {
  public Test07() {
    test(this);
    test();
  }

  // parameters and return values can be raw
  static @Raw Object test(@Raw Object o) {
    return o;
  }

  // return value must be raw since "this" is raw in this context
  // "this" is raw because test() is called from a constructor
  @Raw @RawThis Object test() {
    return this;
  }

  static @RawThis void v() { }
}

