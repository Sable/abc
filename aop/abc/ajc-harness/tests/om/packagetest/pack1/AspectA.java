package pack1;

aspect AspectA {
    before(): call(* A.a()) || call(* pack2.A.a()) {
        System.out.println("pack1.AspectA");
    }
}