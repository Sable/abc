public aspect OrderAspect {


    after(): set(* *.*){
        System.out.println("After 1 in file");
    }

    before(): set(* *.*){
        System.out.println("Before 1 in file");
    }

    after(): set(* *.*){
        System.out.println("After 1 in file");
    }
}
