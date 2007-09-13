interface J {
}
interface I {
}
public class F implements I, J {
    public F(String s) {
        this(s, s);
    }
    public F(String s1, String s2) {
    }
}
aspect Aspect percflow(initialization(*.new(..))) {
}
