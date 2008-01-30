public aspect Aspect {

  before () : within(*) //!within(Aspect) 
    { System.out.println("BEFORE " + thisJoinPointStaticPart.getKind() +
	                 " at " + thisJoinPointStaticPart.getSourceLocation()); 
    }

  after ()  : !within(Aspect) && !handler(*)  
    { System.out.println("AFTER " + thisJoinPointStaticPart.getKind() +
	                 " at " + thisJoinPointStaticPart.getSourceLocation()); 
    }
}
