aspect AspectA {
    before(): call(* A.a(..)) {
        System.out.println("before A.a");
    }
}