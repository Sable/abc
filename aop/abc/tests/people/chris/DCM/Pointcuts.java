package DCM;

public privileged aspect Pointcuts {

  /* Apply to all constructors in user's code */
  public pointcut applConstructors(Object tgt) :
      initialization(Test..*.new(..)) &&
    this(tgt);
    
  public pointcut copyCall() :
  call(* *.copy()) ;
  
  
  public pointcut constrCall() :
     call(*.new(..))  && within(abc..*) /*&& 
     ! call(java..*.new(..)) &&
     ! call(javax..*.new(..)) && 
     ! call(org..*.new(..)) && 
     ! call(DCM..*.new(..)) */&&
     ! call(java_cup.runtime.Symbol.new(..))
     
     ;

     public pointcut start():
     execution (void abc.main.Main.main(..));
     
  /* find all main entry points */
  public pointcut dataOutput() :
      (execution (void abc.main.Main..*(..)))
      ||
      (call (* *.*(..)) && withincode(void abc.main.Main.weave()))
     ;
}
