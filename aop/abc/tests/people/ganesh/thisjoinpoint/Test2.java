public class Test2 {

}

aspect Test2Aspect {
    before() : if(thisJoinPoint!=null && thisJoinPointStaticPart!=null && thisEnclosingJoinPointStaticPart!=null) { }

}
