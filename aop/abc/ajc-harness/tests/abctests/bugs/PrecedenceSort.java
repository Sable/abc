public class PrecedenceSort {
    public static void main(String[] args) {
    }
}

aspect PSA {
    before() : execution(void main(..)) { }
}

aspect PSC {
    before() : execution(void main(..)) { }
}


aspect PSB {
    declare precedence: PSA,PSB;
    declare precedence: PSB,PSC;
    before() : execution(void main(..)) { }
}

