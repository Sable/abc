package test;

public class Test02 {
  // testing inference for various expressions
  void test(boolean condition) {
    // class instantiation
    Test02 l1 = new Test02(), l2 = new Test02(); 
    // conditional expr
    Test02 l3 = condition ? l1 : l2; // @NonNull: both possible values are non-null
    // cast
    Test02 l4 = (Test02)l1; // @NonNull: nullness propagates through casts
    // par expr
    Test02 l5 = (l1); // @NonNull: nullness propagates through par expr
    // string literal
    String l6 = ""; // @NonNull: a string literal is always non-null
    // array creation
    Test02[] l7 = new Test02[1]; // @NonNull: the array is non-null, but the elements may be null
    // string add
    Object l8 = "" + null; // @NonNull: string concatenation always results in a string object
    // class literal
    Object l9 = Test02.class; // @NonNull: the class literal is non-null

    // conditional expr with possibly null value
    Test02 m1 = null, m2 = null; // @Null: null constant
    Test02 m3 = condition ? m1 : m2; // @Null: both possible values are null
    Test02 m4 = condition ? l1 : m1; // @Null: one of the values is null
    // cast
    Test02 m5 = (Test02)m1; // @Null: nullness propagates through casts
    // par expr
    Test02 m6 = (m1); // @NonNull: nullness propagates through par expr
  }

}
