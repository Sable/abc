public aspect OrderAspect {


    after(): set(* *.*){
        System.out.println("After 1 in file");
    }

    before(): set(* *.*){
        System.out.println("Before 1 in file");
    }

    after(): set(* *.*){
        System.out.println("After 2 in file");
    }

    before(): execution(* *.*(..)) {
        System.out.println("An execution aspect is here.");
    }

    before(): preinitialization(*.new(..)) {
        System.out.println("A preinitialization aspect is here.");
    }
}
