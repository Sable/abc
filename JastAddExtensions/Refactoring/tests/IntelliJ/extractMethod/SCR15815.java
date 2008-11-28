public class Foo {
    static Foo
            f1 = new Foo(){
                public String toString() {
                    return /*[*/"a" + "b"/*]*/;
                }
            },
            f2 = new Foo(){};

}