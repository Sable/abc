public aspect Aspect {

  before () : !within(Aspect)  &&  !staticinitialization(*)
    { System.out.println("BEFORE " + thisJoinPointStaticPart.getKind() +
	                 " at " + thisJoinPointStaticPart.getSourceLocation()); 
    }

  after ()  : !within(Aspect) && !handler(*)  && !staticinitialization(*)
    { System.out.println("AFTER " + thisJoinPointStaticPart.getKind() +
	                 " at " + thisJoinPointStaticPart.getSourceLocation()); 
    }
}
