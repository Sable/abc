class A{
    static int field = foo();

    /*[*/static int foo(){
        doSomething();
        return 1;
    }/*]*/

    // added to make it compile
    static void doSomething() { }
}
