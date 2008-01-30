
root module Root {
    open ModuleA;
    constrain ModuleB;
}

module ModuleA {
    class A;
}

module ModuleB {
    friend AspectA;
}

module BadModule {
    open Root;
}