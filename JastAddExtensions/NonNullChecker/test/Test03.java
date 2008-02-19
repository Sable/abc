package test;

public class Test03 {
  public static void main(String[] args) {
    // Subtyping of reference types extended with [NotNull]
    @NonNull Number nNN = null;  // error
    Number n = null;              // ok, null <: Number
    @NonNull Integer iNN = null; // error
    Integer i = null;             // ok, null <: Integer

    nNN = n;                      // error
    n = nNN;                      // ok, [NotNull] Number <: Number
    nNN = iNN;                    // ok, [NotNull] Integer <: [NotNull] Number
    iNN = nNN;                    // error
    nNN = i;                      // error
    n = iNN;                      // ok, [NotNull] Integer <: Number
    n = i;                        // ok, Integer <: Number
  }

}
