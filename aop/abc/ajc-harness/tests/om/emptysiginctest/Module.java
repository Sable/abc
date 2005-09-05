module Module {
    class A;
    module ModuleB;
    __sig {
        method * f1(..);
    }
}

module ModuleB {
    class B;
    constrain module ModuleC;
    __sig {
    }
}

module ModuleC {
    class C;
    __sig {
        method * f3(..);
    }
}