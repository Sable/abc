package DCM;

public aspect Pointcuts {

  /* Apply to all constructors in user's code */
  public pointcut applConstructors(Object tgt) :
      initialization(abc..*.new(..)) &&
    this(tgt);

  /* find all main entry points */
  public pointcut dataOutput() :
      execution (void abc.main.Main.run());
}
