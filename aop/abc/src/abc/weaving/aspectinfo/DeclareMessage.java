package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;

import soot.util.Chain;

import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.WeavingContext;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.LocalGeneratorEx;


/** A <code>declare warning</code> or <code>declare error</code> declaration. */
public class DeclareMessage extends AbstractAdviceDecl {
    public static final int WARNING = 0;
    public static final int ERROR = 1;

    private final String[] sev_name = { "warning", "error" };
    private final int[] polyglot_sev = { ErrorInfo.WARNING, ErrorInfo.SEMANTIC_ERROR };

    private int severity;
    private String message;
    private Aspect aspct;

    public DeclareMessage(int severity, Pointcut pc, String message, Aspect aspct, Position pos) {
	super(null,pc,new ArrayList(),pos);
	this.aspct=aspct;
	this.severity = severity;
	this.message = message;
    }

    public Aspect getAspect() {
	return aspct;
    }

    /** Get the severity of the message.
     *  @return either {@link WARNING} or {@link ERROR}.
     */
    public int getSeverity() {
	return severity;
    }

    /** Get the name of the severity of the message.
     *  @return either <code>&qout;warning&quot;</code> or <code>&qout;error&quot;</code>.
     */
    public String getSeverityName() {
	return sev_name[severity];
    }

    /** Get the message to give if the pointcut matches anything. */
    public String getMessage() {
	return message;
    }

    public String toString() {
	return "declare "+sev_name[severity]+": "+pc+": \""+message+"\";";
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" special: declare "+getSeverityName()+" : "+getMessage());
    }

    public WeavingEnv getWeavingEnv() {
	return new EmptyFormals();
    }

    public WeavingContext makeWeavingContext() {
	throw new InternalCompilerError
	    ("declare warning/error should never make it past the matcher");
    }

    public Residue postResidue(ShadowMatch sm) {
	if(abc.main.Main.v()==null) throw new InternalCompilerError("main was null");
	if(abc.main.Main.v().error_queue==null) throw new InternalCompilerError("no error queue");
	abc.main.Main.v().error_queue.enqueue
	    (ErrorInfoFactory.newErrorInfo
	     (polyglot_sev[severity],
	      message,
	      sm.getContainer(),
	      sm.getHost()));

	return NeverMatch.v;
    }

    public Chain makeAdviceExecutionStmts
	(LocalGeneratorEx localgen,WeavingContext wc) {
	throw new InternalCompilerError
	    ("declare warning/error should never make it past the matcher");
    }
}
