/**
 * An implementation of the LoD check using just two
 * concrete aspects, with two 2-line advise each (and
 * a few short auxiliary methods.)
 * 
 * @authors David H. Lorenz and Pengcheng Wu
 * @version 0.4, 12/19/02
 */
/*** THIS MUST BE LINE 9 ***/
package lawOfDemeter;
public abstract class Any {
  public pointcut scope(): !within(lawOfDemeter..*) 
    && 
    !cflow(withincode(* lawOfDemeter..*(..))) 
    ;
  public pointcut StaticInitialization(): scope()
    && staticinitialization(*);
  public pointcut MethodCallSite(): scope()
    && call(* *(..));
  public pointcut ConstructorCall(): scope()
    && call(*.new (..));
  public pointcut MethodExecution(): scope()
    && execution(* *(..)); 
  public pointcut ConstructorExecution(): scope()
    && execution(*.new (..));
  public pointcut Execution(): 
    ConstructorExecution() || MethodExecution(); 
  public pointcut MethodCall(Object thiz, Object targt): 
     MethodCallSite() && this(thiz) && target(targt);
  public pointcut SelfCall(Object thiz, Object targt): 
    MethodCall(thiz,targt) && if(thiz == targt);
  public pointcut StaticCall(): scope() 
    && call(static * *(..));
  public pointcut Set(Object value): scope() 
    && set(* *.*) && args(value); 
  public pointcut Initialization(): scope()
    && initialization(*.new(..)); 
}
