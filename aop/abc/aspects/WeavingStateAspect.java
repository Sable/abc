import java.io.*;
import java.util.*;

public privileged aspect WeavingStateAspect {
    Set fieldSet = new HashSet();
    PrintWriter fr;
    public WeavingStateAspect() {
        try {
            fr = new PrintWriter(new FileWriter("weaving_side_effects.txt", true));
        } catch(IOException e) {
            throw new RuntimeException("can't open file");
        }
    }

    pointcut setpc(): set(* *.*); //  && !set(* abc.weaving.weaver.WeavingContext+.*);
    pointcut inweaver(): cflow(execution(void abc.weaving.weaver.Weaver.weaveAdvice()));
    pointcut modify_collection():
               call(public * *(..))
            && !call(* size(..))
            && !call(* contains(..))
            && !call(* containsAll(..))
            && !call(* equals(..))
            && !call(* hashCode(..))
            && !call(* isEmpty(..))
            && !call(* iterator(..))
            && target(Collection+)
            && !cflow(execution(public * *(..)) && this(Collection+))
            && !cflow(execution(void foo(String)))
            ;

    pointcut not_in_grammar():
        !within(abc.aspectj.parse..*);
        //&& !within(abc.eaj.parse..*);

    before(): (setpc() || modify_collection()) && inweaver() && not_in_grammar() {
        String sjp = "+++ "+thisJoinPointStaticPart.getSignature().toString()+"\n"+
            "++- "+thisEnclosingJoinPointStaticPart.getSignature().toString();
        foo(sjp);
    }
    public void foo(String sjp) {
        if( fieldSet.add(sjp) ) {
            fr.println("============================================================================");
            fr.println( sjp );
            fr.println(stackTrace());
            fr.flush();
        }
    }
    private String stackTrace() {
        Throwable t = new Throwable();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.printStackTrace( new PrintStream(baos) );
        return baos.toString();
    }
}
