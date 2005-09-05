
module ModuleA {
    class A;
    module ModuleB;
    module ModuleC;
    __sig {
        method * f1(..);
    }
}

module ModuleB {
    class B;
    __sig {
        method * f2(..);
    }
}

module ModuleC {
    class C;
    __sig {
        method * f3(..);
    }
}