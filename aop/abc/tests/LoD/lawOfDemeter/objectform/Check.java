package lawOfDemeter.objectform;
import lawOfDemeter.Any;
import org.aspectj.lang.JoinPoint;
import java.util.*;
/**
 * @authors David H. Lorenz and Pengcheng Wu
 * @version 0.7, 12/01/02
 */
/*** THIS MUST BE LINE 9 ***/
aspect Check {
  private pointcut IgnoreCalls():
    call(* java..*.*(..));

  private pointcut IgnoreTargets():
    get(static * java..*.*);

  after() returning(Object o):IgnoreTargets() {
    ignoredTargets.put(o,o);
  }

  after(Object thiz,Object targt):
    Any.MethodCall(thiz,targt)
    && !IgnoreCalls() {
    if (!ignoredTargets.containsKey(targt) &&
      !Pertarget.aspectOf(thiz).contains(targt))
    { objectViolations.put(thisJoinPointStaticPart,
	                   thisJoinPointStaticPart);
    }
  }

  after(): execution(void *.main(String[])) {
    Collection c = objectViolations.values();
    for (Iterator i = c.iterator(); i.hasNext(); )
     { JoinPoint.StaticPart jp = (JoinPoint.StaticPart) i.next();
       System.out.println(
         " !! LoD Object Violation !! " + jp + at(jp)); 
     }
  }

  private String at(JoinPoint.StaticPart jp) {
    return " at " + jp.getSourceLocation().getFileName()
           + ":" + jp.getSourceLocation().getLine();
  }

  private IdentityHashMap 
   ignoredTargets = new IdentityHashMap();

  private IdentityHashMap 
    objectViolations = new IdentityHashMap();

}
