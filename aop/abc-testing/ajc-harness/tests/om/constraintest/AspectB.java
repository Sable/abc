aspect AspectB {
    before() : call(* f1(..)) || call(* f2(..)) || call(* f3(..)) {
        System.out.println("AspectB");
    }
}