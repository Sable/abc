public class Conflict { 
  Conflict(String[] args) { }

  public static void main(String[] args) { new Conflict(args); } 

}

abstract aspect ConflictBase {
  abstract pointcut details(Object x);
  before(Object x): execution(Conflict.new(..)) && details(x) { System.out.println(x); }

}

aspect Conflict1 extends ConflictBase {
  pointcut details(Object x): this(x);
}

aspect Conflict2 extends ConflictBase {
  pointcut details(Object x): args(x);
}
