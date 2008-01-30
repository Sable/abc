public class Test2 {

}

aspect Test2Aspect {
    before(Object x) : this(x) && if(x!=null && thisJoinPoint!=null && thisJoinPointStaticPart!=null && thisEnclosingJoinPointStaticPart!=null) { System.out.println(thisJoinPoint); }
}
