import polyglot.util.InternalCompilerError;

public aspect NullResidue {
    after () returning(Residue r):execution(* *.*(..)) {
        throw new InternalCompilerError("null residue returned");
    }
}