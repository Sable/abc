public aspect Aspect {

  before () : !within(Aspect) 
    { System.out.print("BEFORE " + thisJoinPointStaticPart.toLongString()); 
      System.out.println(" at " + thisJoinPointStaticPart.getSourceLocation());
    }

  after () : !within(Aspect) 
    { System.out.print("AFTER " + thisJoinPointStaticPart.toLongString()); 
      System.out.println(" at " + thisJoinPointStaticPart.getSourceLocation());
    }
}
