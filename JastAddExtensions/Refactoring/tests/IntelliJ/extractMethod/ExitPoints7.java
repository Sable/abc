class Foo {
   public Object createBean(Object parent) {
    try {
      /*[*/if (parent != null) {
        try {
        }
        catch (Exception e) {
          return null;
        }
      }


      Object tag = null;/*]*/

      tag = foo(tag);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  private Object foo(final Object tag) {
    return null;
  }
}