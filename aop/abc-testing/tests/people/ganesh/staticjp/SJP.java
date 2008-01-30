import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.CatchClauseSignature;

public class SJP {

   int x;
   SJP() {
      this(5,3);
   }

   SJP(int y,int z) {
     x=y;
   }

   int foo() {
     return x;
   }
  
   int bar() {
     try {
        try {
           throw new RuntimeException("");
        } catch(RuntimeException e) {
          System.out.println(e);
          throw new Error("");
        }
     } catch(Error e) {
       // ajc loses the name of the variable caught if it isn't used at all
       return foo();
     }
   }

   public static void main(String[] args) throws Exception {
      new SJP().bar();
   }
}

aspect Asp1 {
   before() : !within(Asp1) && !within(Asp2) {
      Asp1.dump(thisJoinPointStaticPart);
   }

   static void dump(JoinPoint.StaticPart sjp) {
     System.out.println(sjp);
     Signature sig=sjp.getSignature();
     System.out.println(sig.toLongString());
     if(sig instanceof CodeSignature) {
        CodeSignature csig=(CodeSignature) sig;
        String[] names=csig.getParameterNames();
        for(int i=0;i<names.length;i++) System.out.println(names[i]);
        Class[] excs=csig.getExceptionTypes();
        for(int i=0;i<excs.length;i++) System.out.println(excs[i]);
     }
     if(sig instanceof CatchClauseSignature)
        System.out.println(((CatchClauseSignature) sig).getParameterName());
   }        
}

aspect Asp2 {
   static boolean doneit=false;
   before() : within(Asp1) && adviceexecution() && if(!doneit)  {
     doneit=true;
     Asp1.dump(thisJoinPointStaticPart);
   }
}
