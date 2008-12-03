public class SuperClass {
    /*[*/public void doSomething() {
        UtilClass.doSomething(this);
    }/*]*/
}

public class SubClass extends SuperClass {
    public void doSomethingElse() {
        doSomething();
    }
}

public class UtilClass {
    public static void doSomething(SuperClass superClass) {
        // ...
    }
}