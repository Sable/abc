import polyglot.util.InternalCompilerError;
import abc.weaving.residues.Residue;

public aspect NullResidue {
    after () returning(Residue r):execution(* *.*(..)) {
        throw new InternalCompilerError("null residue returned");
    }
}
