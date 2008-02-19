package test;

public class Test01 {

  // test local variable inference
  void test() {
    Test01 l1 = new Test01(); // @NonNull: non-null constant
    Test01 l2 = l1; // @NonNull: other non-null variable
    Test01 l3 = new Test01();
    l3 = l3; // @NonNull: non-null constant + circular 
    Test01 l4 = l1;
    l4 = l3; // @NonNull: assigned two non-null values
    Test01 l5 = new Test01();
    Test01 l6 = new Test01();
    l5 = l6;
    l6 = l5; // @NonNull: mutually dependent variables

    Test01 m1 = null; // @Null: null constant
    Test01 m2 = m1; // @Null: other null variable
    Test01 m3 = null;
    m3 = m3; // @Null: null constant + circular
    Test01 m4 = m1;
    m4 = m3; // @Null: assigned two null values
    Test01 m5 = l1;
    m5 = m1; // @Null: non-null + null = null;

    Test01 m6 = null;
    Test01 m7 = null;
    m6 = m7;
    m7 = m6; // @Null: mutually dependent variables

    Test01 m8 = new Test01();
    Test01 m9 = new Test01();
    m8 = m9;
    m9 = m8;
    m8 = null; // @Null: mutually dependent variables with a single null
  }
}
