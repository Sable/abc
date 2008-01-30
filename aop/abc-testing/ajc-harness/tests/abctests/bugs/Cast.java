/*

To make cast.jar:

abc Cast.java
soot -f jasmin -d . Cast
perl -ne 'print unless /checkcast/;' -i Cast.jasmin
java jasmin.Main -d . Cast.jasmin
jar -cvf cast.jar Cast.class IA.class IB.class IC.class ID.class CC.class CD.class

Then break it with

abc -ext abc.eaj -injars cast.jar CastAspect.java
java Cast

*/


import org.aspectj.testing.Tester;

public class Cast {
    public static void main(String[] args) {
        // Need to test this by modifying the bytecode produced
        Object x;
        if(args.length>=2) x=new CC(); else x=new CD();
        ((IA) x).f();
        ((IB) x).g();
        Tester.checkAllEvents();
    }
}

interface IA {
    void f();
}

interface IB {
    void g();
}

interface IC extends IA,IB { }

class CC implements IC {
    public void f() {}
    public void g() {}
}

interface ID extends IA,IB { }

class CD implements ID {
    public void f() {}
    public void g() {}
}