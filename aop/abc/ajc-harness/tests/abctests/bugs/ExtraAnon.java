public abstract class ExtraAnon {
    abstract void foo();
}

class E$A2 {
    ExtraAnon ea() {
        return new ExtraAnon() { void foo() {} };
    }
}