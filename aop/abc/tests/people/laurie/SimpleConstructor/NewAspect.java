public aspect NewAspect {
  before():  call(*.new(..)) { System.out.println("before constructor"); }
  after () : call(*.new(..)) { System.out.println("after constructor"); }
}
