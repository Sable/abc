package test;

public class Test05 {
  public static void main(String[] args) {
    // [NotNull] instance fields must be initialized after each constructor
  }

  @NonNull String n1 = "n1"; // ok, field is initialized at declaration
  @NonNull String n2; // ok, field is initialized in each constructor
  @NonNull String n3; // ok, field is initialized in instance initialized
  @NonNull String n4; // error, field is only initialized in one constructor
  @NonNull String n5; // ok, field is initialized in one constructor that is called by the other constructor

  public Test05() {
    this(0);
    n2 = "n2";
    n4 = "n4";
  }

  public Test05(int i) {
    super();
    n2 = "n2";
    n5 = "n5";
  }

  {
    n3 = "n3";
  }
}
