package abc.tm.weaving.matching;

/** 
 * An edge in the state machine. Has source and target nodes and edge label.
 * 
 * Null labels mean epsilon transitions.
 *
 *  @author Pavel Avgustinov
 */

public class SMEdge {

    protected SMNode source, target;
    // TODO: Is SymbolDecl really the type to choose?
    protected String label;
    
    public SMEdge(SMNode from, SMNode to, String l) {
        source = from;
        target = to;
        label = l;
    }
    
    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * @return Returns the source.
     */
    public SMNode getSource() {
        return source;
    }
    /**
     * @param source The source to set.
     */
    public void setSource(SMNode source) {
        this.source = source;
    }
    /**
     * @return Returns the target.
     */
    public SMNode getTarget() {
        return target;
    }
    /**
     * @param target The target to set.
     */
    public void setTarget(SMNode target) {
        this.target = target;
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof SMEdge)) return false;
        SMEdge edge = (SMEdge) o;
        return (edge.getSource() == this.source && edge.getTarget() == this.target &&
                edge.getLabel() == this.label);
    }
}
