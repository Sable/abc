package test;

// test return value non-null inference
public class Test05 {
  Object test1() { // @NonNull: return value is non-null
    return new Test05();
  }
  Object test2(boolean condition) { // @NonNull: all returned values are non-null
    if(condition)
      return new Test05();
    else
      return new Test05();
  }
  Object test3(boolean condition) { // @Null: one possibly null value
    if(condition)
      return new Test05();
    else
      return null;
  }

  // nullness on return values is invariant for subtyping
  
  // subtyping through subclassing
  Object test4() { // @NonNull: both methods return non-null values
    return new Test05();
  }
  Object test5() { // @Null: overriding method return null value
    return new Test05();
  }
  Object test6() { // @Null: method returns null value
    return null;
  }
  static class SubTest05 extends Test05 {
    Object test4() { // @NonNull: both methods return non-null values
      return new Test05();
    }
    Object test5() { // @Null: method returns null value
      return null;
    }
    Object test6() { // @NonNull: overridden method returns null value
                     // but using covariant return types
      return new Test05();
    }
  }

  // subtyping through interfaces
  interface I7 {
    Object test7(); // @NonNull: both implementations return non-null values
  }
  interface I8 {
    Object test8(); // @Null: implementation in X returns null value
  }
  interface I9 {
    Object test9(); // @Null: implementation in Y returns null value
  }
  static class X implements I7, I8, I9 {
    public Object test7() {
      return new Test05(); // @NonNull: both implementations return non-null value
    }
    public Object test8() { // @Null: returns null locally
      return null;
    }
    public Object test9() { // @NonNull: implementation in Y return null value
                            // but uses covariant return types
      return new Test05();
    }
  }
  static class Y implements I7, I8, I9 {
    public Object test7() { // @NonNull: both implementations return non-null value
      return new Test05();
    }
    public Object test8() { // @NonNull: implementation in X return null value
                            // but uses covariant return types
      return new Test05();
    }
    public Object test9() { // @Null: returns null locally
      return null;
    }
  }


}
    
 
