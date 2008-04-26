package test;

public class Test01 {
  
  public static void main(String[] args) {
    // Test syntax for @NonNull annotations
    // annotations are allowed for reference types in
    // local variables, parameters, fields, and method declarations.
    @NonNull Object okVariable;
    @NonNull int errorVariable;
  }

  @NonNull int errorField;
  @NonNull Object okField = new Object();

  @NonNull String okMethodandParameter(@NonNull String arg) {
    return "";
  }

  @NonNull void errorMethod() {
  }

  void errorParameter(@NonNull int i) {
  }
    
}
