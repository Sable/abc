public class ReturningVoid {

    void v() { }

}

aspect RVAspect {

    after () returning (Object o) : execution(void v()) { }
    after () returning (Integer i) : execution(void v()) { }

}
