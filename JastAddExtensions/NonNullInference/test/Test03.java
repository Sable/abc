package test;

// test if-stmt as safe cast to non-null
public class Test03 {

  Test03 field1 = null;
  Test03 field2 = new Test03();
  public void test(Test03 p1, Test03 p2) {
    // make sure p1 is possibly-null and p2 is non-null
    test(null, new Test03());
    Test03 l1 = null;
    Test03 l2 = new Test03();
  
    // comparison to non-null turns variables and parameters
    // to non-null in the then branch
    if(l1 != null) {
      Test03 m1 = l1; // @NonNull: explicit check for non-null variable
    }
    if(p1 != null) {
      Test03 m1 = p1; // @NonNull: explicit check for non-null parameter
    }
    if(field1 != null) {
      Test03 m2 = field1; // @Null: fields can be mutated by other threads
    }
    
    // check symmetry in comparison
    if(null != l1) {
      Test03 m1 = l1; // @NonNull: explicit check for non-null variable
    }

    // comparion to null turns variables and parameters
    // to non-null in the else branch
    if(l1 == null) ;
    else {
      Test03 m1 = l1; // @NonNull: explicit check for non-null variable
    }
    if(p1 == null) ;
    else {
      Test03 m1 = p1; // @NonNull: explicit check for non-null parameter
    }
    if(field1 == null) ;
    else {
      Test03 m2 = field1; // @Null: fields can be mutated by other threads
    }
    
    // check symmetry in comparison
    if(null == l1) ;
    else {
      Test03 m1 = l1; // @NonNull: explicit check for non-null variable
    }
 
    // check that all assignments in the conditional branch are non-null
    // notice that the assignments do not actually have to be in the control flow to m1
    if(l1 != null) {
      l1 = new Test03();
      Test03 m1 = l1; // @NonNull: explicit check for non-null variable and all assignments are non-null
      l1 = new Test03();
    }
    if(l1 != null) {
      l1 = new Test03();
      Test03 m1 = l1; // @Null: explicit check for non-null variable and there exist a null assignment
      l1 = null;
    }
  }

}
