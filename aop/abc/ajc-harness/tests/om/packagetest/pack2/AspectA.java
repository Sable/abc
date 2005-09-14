package pack2;

aspect AspectA {
    before(): call(* A.a()) {
        System.out.println("pack2.AspectA");
    }
}