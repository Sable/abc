import org.aspectj.testing.Tester;

public class InsideIf {
    public static void main(String[] args) {
       new InsideIf();
       Tester.expectEvent("ran advice");
       Tester.checkAllEvents();
    }
}

aspect InsideIfAspect {
    before(Object x) : execution(InsideIf.new()) &&
                       this(x) && if(x!=null && thisJoinPoint!=null && 
                                     thisJoinPointStaticPart!=null && 
                                     thisEnclosingJoinPointStaticPart!=null) 
        { 
           Tester.event("ran advice"); 
        }

}
