
package abc.weaving.aspectinfo;

import polyglot.util.Position;

/** A <code>declare warning</code> or <code>declare error</code> declaration. */
public class DeclareMessage extends InAspect {
    public static final int WARNING = 0;
    public static final int ERROR = 1;

    private final String[] sev_name = { "warning", "error" };

    private int severity;
    private Pointcut pc;
    private String message;

    public DeclareMessage(int severity, Pointcut pc, String message, Aspect aspct, Position pos) {
	super(aspct, pos);
	this.severity = severity;
	this.pc = pc;
	this.message = message;
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

    /** Get the pointcut to check for matches. */
    public Pointcut getPointcut() {
	return pc;
    }

    /** Get the message to give if the pointcut matches anything. */
    public String getMessage() {
	return message;
    }

    public String toString() {
	return "declare "+sev_name[severity]+": "+pc+": \""+message+"\";";
    }

}
