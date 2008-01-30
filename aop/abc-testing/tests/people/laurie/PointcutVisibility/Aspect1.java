public aspect Aspect1 {
  before (): Main.inmain() {
    System.out.println("BEFORE 1: This join point " + thisJoinPointStaticPart +
                        thisJoinPointStaticPart.getSourceLocation());
  }

  before (): A.inA() {
    System.out.println("BEFORE 2: This join point " + thisJoinPointStaticPart +
                        thisJoinPointStaticPart.getSourceLocation());
  }

  before (): !within(Aspect*) {
    System.out.println("BEFORE 3: This join point " + thisJoinPointStaticPart +
                        thisJoinPointStaticPart.getSourceLocation());
  }
}
