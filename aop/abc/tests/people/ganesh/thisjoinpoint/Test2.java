public class Test2 {

}

aspect Test2Aspect {
    before() : if(true) { if(thisJoinPoint==null) { } }

}
