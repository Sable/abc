package abc.polyglot.util;

import polyglot.util.Position;
import polyglot.util.ErrorInfo;
import soot.SootMethod;
import soot.tagkit.*;

public class ErrorInfoFactory {
    public static ErrorInfo newErrorInfo(int kind,String message,SootMethod container,Host host) {
	Position pos=null;
	if(container.getDeclaringClass().hasTag("SourceFileTag")) {
	    SourceFileTag sfTag=(SourceFileTag) 
		container.getDeclaringClass().getTag("SourceFileTag");
	    if(host.hasTag("SourceLnPosTag")) {
		SourceLnPosTag slpTag=(SourceLnPosTag) host.getTag("SourceLnPosTag");
		pos=new Position(sfTag.getSourceFile(),
				 slpTag.startLn(),slpTag.startPos(),
				 slpTag.endLn(),slpTag.endPos());
	    } else {
		pos=new Position(sfTag.getSourceFile());
		message+=" in method "+container;
	    }
	} else {
	    message+=" in method "+container
		+" in class "+container.getDeclaringClass();
	}

	return new ErrorInfo(kind,message,pos);
    }
}
