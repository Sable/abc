package DCM;

public aspect Pointcuts {

  /* Apply to all constructors in user's code */
  public pointcut applConstructors(Object tgt) :
    execution(*.new(..)) && 
    !execution(java..*.new(..)) && 
    !execution(javax..*.new(..)) &&
    !execution(org..*.new(..)) && 
    !execution(DCM..new(..)) &&
    target(tgt);

  /* find all main entry points */
  public pointcut dataOutput() :
    execution (static void *.main(String[]));
}
