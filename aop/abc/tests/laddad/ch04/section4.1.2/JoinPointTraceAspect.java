//Listing 4.2 JoinPointTraceAspect.java

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

public aspect JoinPointTraceAspect {
    private int _indent = -1;

    pointcut tracePoints() :
	!within(JoinPointTraceAspect)
	&& !call(*.new(..)) && !execution(*.new(..))
	&& !initialization(*.new(..)) && !staticinitialization(*);

    before() : tracePoints() {
	_indent++;
	println("========= " + thisJoinPoint + " ===========");
	println("Dynamic join point information:");
	printDynamicJoinPointInfo(thisJoinPoint);
	println("Static join point information:");
	printStaticJoinPointInfo(thisJoinPointStaticPart);
	println("Enclosing join point information:");
	printStaticJoinPointInfo(thisEnclosingJoinPointStaticPart);
    }

    after() : tracePoints() {
	_indent--;
    }

    private void printDynamicJoinPointInfo(JoinPoint joinPoint) {
	println("This: " + joinPoint.getThis() +
		" Target: " + joinPoint.getTarget());
	StringBuffer argStr = new StringBuffer("Args: ");
	Object[] args = joinPoint.getArgs();
	for (int length = args.length, i = 0; i < length; ++i) {
	    argStr.append(" [" + i + "] = " + args[i]);
	}
	println(argStr);
    }

    private void printStaticJoinPointInfo(
        JoinPoint.StaticPart joinPointStaticPart) {
	println("Signature: " + joinPointStaticPart.getSignature()
		+ " Kind: " + joinPointStaticPart.getKind());
	SourceLocation sl = joinPointStaticPart.getSourceLocation();
	println("Source location: " +
		sl.getFileName() + ":" + sl.getLine());
    }

    private void println(Object message) {
	for (int i = 0, spaces = _indent * 2; i < spaces; ++i) {
	    System.out.print(" ");
	}
	System.out.println(message);
    }
}
